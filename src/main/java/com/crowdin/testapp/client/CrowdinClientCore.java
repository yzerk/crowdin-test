package com.crowdin.testapp.client;

import com.crowdin.client.core.http.exceptions.HttpBadRequestException;
import com.crowdin.client.core.http.exceptions.HttpException;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;


abstract class CrowdinClientCore {

    public static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages/messages");

    private static final Map<BiPredicate<String, String>, RuntimeException> standardErrorHandlers =
            new LinkedHashMap<BiPredicate<String, String>, RuntimeException>() {{
                put((code, message) -> code.equals("401"),
                        new RuntimeException(RESOURCE_BUNDLE.getString("error.response.401")));
                put((code, message) -> code.equals("403"),
                        new RuntimeException(RESOURCE_BUNDLE.getString("error.response.403")));
                put((code, message) -> code.equals("404") && StringUtils.containsIgnoreCase(message, "Project Not Found"),
                        new RuntimeException(RESOURCE_BUNDLE.getString("error.response.404_project_not_found")));
                put((code, message) -> code.equals("404") && StringUtils.containsIgnoreCase(message, "Organization Not Found"),
                        new RuntimeException(RESOURCE_BUNDLE.getString("error.response.404_organization_not_found")));
                put((code, message) -> code.equals("notUnique"),
                        new RuntimeException(RESOURCE_BUNDLE.getString("error.response.notUnique")));
                put((code, message) -> StringUtils.containsAny(message,
                                "PKIX path building failed",
                                "sun.security.provider.certpath.SunCertPathBuilderException",
                                "unable to find valid certification path to requested target"),
                        new RuntimeException(RESOURCE_BUNDLE.getString("error.response.certificate")));
                put((code, message) -> message.equals("Name or service not known"),
                        new RuntimeException(RESOURCE_BUNDLE.getString("error.response.url_not_known")));
                put((code, message) -> code.equals("<empty_code>") && message.equals("<empty_message>"),
                        new RuntimeException("Empty error message from server"));
            }};

    protected static <T> T executeRequest(Supplier<T> r) {
        return executeRequest(new HashMap<BiPredicate<String, String>, RuntimeException>(), r);
    }

    protected static <T, R extends Exception> T executeRequest(Map<BiPredicate<String, String>, R> errorHandlers, Supplier<T> r) throws R {
        try {
            return r.get();
        } catch (HttpBadRequestException e) {
            for (HttpBadRequestException.ErrorHolder eh : e.getErrors()) {
                for (HttpBadRequestException.Error error : eh.getError().errors) {
                    String code = (error.code != null) ? error.code : "<empty_code>";
                    String message = (error.message != null) ? error.message : "<empty_message>";
                    searchErrorHandler(errorHandlers, code, message);
                }
            }
            String errorMessage = "Wrong parameters: \n" + e.getErrors()
                    .stream()
                    .flatMap(holder -> holder.getError().getErrors()
                            .stream()
                            .map(error -> String.format("<key: %s, code: %s, message: %s>", holder.getError().getKey(), error.getCode(), error.getMessage())))
                    .collect(Collectors.joining("\n"));
            throw new RuntimeException(errorMessage);
        } catch (HttpException e) {
            String code = (e.getError() != null && e.getError().code != null) ? e.getError().code : "<empty_code>";
            String message = (e.getError() != null && e.getError().message != null) ? e.getError().message : "<empty_message>";
            searchErrorHandler(errorHandlers, code, message);
            throw new RuntimeException(String.format("Error from server: <Code: %s, Message: %s>", code, message));
        } catch (Exception e) {
            throw e;
        }
    }

    private static <T, R extends Exception> void searchErrorHandler(Map<BiPredicate<String, String>, R> errorHandlers, String code, String message) throws R {
        for (Map.Entry<BiPredicate<String, String>, R> errorHandler : errorHandlers.entrySet()) {
            if (errorHandler.getKey().test(code, message)) {
                throw errorHandler.getValue();
            }
        }
        for (Map.Entry<BiPredicate<String, String>, RuntimeException> errorHandler : standardErrorHandlers.entrySet()) {
            if (errorHandler.getKey().test(code, message)) {
                throw errorHandler.getValue();
            }
        }
    }

}
