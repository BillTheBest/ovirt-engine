package org.ovirt.engine.api.restapi.resource;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.ovirt.engine.api.common.invocation.Current;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.common.util.EnumValidator;
import org.ovirt.engine.api.common.util.CompletenessAssertor;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.api.restapi.logging.MessageBundle;
import org.ovirt.engine.api.restapi.logging.Messages;
import org.ovirt.engine.api.restapi.util.SessionHelper;

public class BaseBackendResource {

    protected static final LogCompat LOG = LogFactoryCompat.getLog(AbstractBackendResource.class);

    protected BackendLocal backend;
    protected SessionHelper sessionHelper;
    protected MessageBundle messageBundle;
    protected UriInfo uriInfo;
    protected HttpHeaders httpHeaders;

    public void setBackend(BackendLocal backend) {
        this.backend = backend;
    }

    public BackendLocal getBackend() {
        return backend;
    }

    public void setSessionHelper(SessionHelper sessionHelper) {
        this.sessionHelper = sessionHelper;
    }

    public SessionHelper getSessionHelper() {
        return sessionHelper;
    }

    public void setMessageBundle(MessageBundle messageBundle) {
        this.messageBundle = messageBundle;
    }

    public MessageBundle getMessageBundle() {
        return messageBundle;
    }

    public UriInfo getUriInfo() {
        return uriInfo;
    }

    @Context
    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    public HttpHeaders getHttpHeaders() {
        return httpHeaders;
    }

    @Context
    public void setHttpHeaders(HttpHeaders httpHeaders) {
        this.httpHeaders = httpHeaders;
    }

    protected Current getCurrent() {
        return sessionHelper.getCurrent();
    }

    protected <P extends VdcQueryParametersBase> P sessionize(P parameters) {
        return sessionHelper.sessionize(parameters);
    }

    protected <P extends VdcActionParametersBase> P sessionize(P parameters) {
        return sessionHelper.sessionize(parameters);
    }

    protected Fault fault(String reason, String detail) {
        Fault fault = new Fault();
        fault.setReason(reason);
        fault.setDetail(detail);
        return fault;
    }

    static String detail(Throwable t) {
        String detail = null;
        if (LOG.isDebugEnabled()) {
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw, true));
            detail = sw.toString();
        } else {
            detail = t.getMessage();
        }
        return detail;
    }

    /**
     * An exception which may be thrown from a BackendOperation invoke()
     * method with a message containing details of the operation failure.
     */
    protected static class BackendFailureException extends Exception {

        private static final long serialVersionUID = 2244591834711331403L;

        public BackendFailureException(String failure) {
            super(failure);
        }
    }

    /**
     * A BackendFailureException subclass specifically indicating that
     * the entity targeted by the operation does not exist.
     */
    protected class EntityNotFoundException extends BackendFailureException {

        private static final long serialVersionUID = -761673260081428877L;
        private String identifier;

        public EntityNotFoundException(String identifier) {
            super(localize(Messages.ENTITY_NOT_FOUND_TEMPLATE, identifier));
            this.identifier = identifier;
        }

        public String getIdentifier() {
            return identifier;
        }
    }

    protected class WebFaultException extends WebApplicationException {

        private static final long serialVersionUID = 394735369823915802L;
        private Fault fault;

        public WebFaultException(Exception cause, String detail, Status status) {
            this(cause, localize(Messages.BACKEND_FAILED), detail, status);
        }

        public WebFaultException(Exception cause, String reason, String detail, Status status) {
            this(cause, fault(reason, detail), status);
        }

        public WebFaultException(Exception cause, Fault fault, Status status) {
            super(cause, Response.status(status).entity(fault).build());
            this.fault = fault;
        }

        public Fault getFault() {
            return fault;
        }
    }

    /**
     * Handle a BackendFailureException or an exception thrown from a
     * backend query/action and re-throw as a WebApplicationException.
     *
     * If the exception indicates that the referenced backend entity
     * does not exist and @notFoundAs404 is true, then throw a
     * WebApplicationException which wraps a 404 HTTP response.
     * @param e the exception to handle
     * @param notFoundAs404 whether to return a 404 if appropriate
     *
     * @returns the result of the operation
     */
    protected <T> T handleError(Exception e, boolean notFoundAs404) {
        handleError(Void.class, e, notFoundAs404);
        return null;
    }

    /**
     * Handle a BackendFailureException or an exception thrown from a
     * backend query/action and re-throw as a WebApplicationException.
     *
     * If the exception indicates that the referenced backend entity
     * does not exist and @notFoundAs404 is true, then throw a
     * WebApplicationException which wraps a 404 HTTP response.
     * @param clz dummy explicit type parameter for use when type
     * inference is not possible (irrelevant in any case as a value
     * is never returned, rather an exception is always thrown)
     * @param e the exception to handle
     * @param notFoundAs404 whether to return a 404 if appropriate
     *
     * @returns the result of the operation
     */
    protected <T> T handleError(Class<T> clz, Exception e, boolean notFoundAs404) {
        if ((e instanceof EntityNotFoundException) && (notFoundAs404)) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
        } else if ((e instanceof BackendFailureException) && (!StringHelper.isNullOrEmpty(e.getMessage()))) {
            LOG.errorFormat(localize(Messages.BACKEND_FAILED_TEMPLATE), e.getMessage(), null);
            throw new WebFaultException(null, e.getMessage(), Response.Status.BAD_REQUEST);
        } else {
            LOG.errorFormat(localize(Messages.BACKEND_FAILED_TEMPLATE), detail(e), e);
            throw new WebFaultException(e, detail(e), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> List<T> asCollection(Class<T> clz, Object o) {
        List<T> collection = null;
        if (o instanceof List) {
            collection = (List<T>)o;
        } else if (clz.isInstance(o)) {
            collection = new ArrayList<T>();
            collection.add(clz.cast(o));
        }
        return collection;
    }

    static <T> ArrayList<T> asList(T t) {
        ArrayList<T> list = new ArrayList<T>();
        list.add(t);
        return list;
    }

    protected Guid asGuid(String id) {
        return new Guid(id);
    }

    protected Guid asGuid(NGuid nGuid) {
        return new Guid(nGuid.getUuid());
    }

    protected <T> T instantiate(Class<T> clz) {
        T ret = null;
        try {
            ret = clz.newInstance();
        } catch (Exception e) {
            // simple instantiation shouldn't fail
        }
        return ret;
    }

    protected Locale getEffectiveLocale() {
        List<Locale> locales = httpHeaders.getAcceptableLanguages();
        return locales != null && locales.size() > 0
               ? locales.get(0)
               : null;
    }

    protected String localize(Messages message, Object... parameters) {
        Locale locale = getEffectiveLocale();
        return locale != null
               ? messageBundle.localize(message, locale, parameters)
               : messageBundle.localize(message, parameters);
    }

    protected String localize(String error) {
        Locale locale = getEffectiveLocale();
        return locale != null
               ? backend.getErrorsTranslator().TranslateErrorTextSingle(error, locale)
               : backend.getErrorsTranslator().TranslateErrorTextSingle(error);
    }

    protected String localize(List<String> errors) {
        Locale locale = getEffectiveLocale();
        return locale != null
               ? backend.getErrorsTranslator().TranslateErrorText(errors, locale).toString()
               : backend.getErrorsTranslator().TranslateErrorText(errors).toString();
    }

    public void validateParameters(Object model, String... required) {
        validateParameters(model, 2, required);
    }

    public void validateParameters(Object model, int frameOffset, String... required) {
        String reason = localize(Messages.INCOMPLETE_PARAMS_REASON);
        String detail = localize(Messages.INCOMPLETE_PARAMS_DETAIL_TEMPLATE);
        CompletenessAssertor.validateParameters(reason, detail, model, frameOffset + 1, required);
    }

    public <E extends Enum<E>> E validateEnum(Class<E> clz, String name) {
        String reason = localize(Messages.INVALID_ENUM_REASON);
        String detail = localize(Messages.INVALID_ENUM_DETAIL);
        return EnumValidator.validateEnum(reason, detail, clz, name);
    }

    protected String combine(String head, String tail) {
        if (head.endsWith("/")) {
            head = head.substring(0, head.length() - 1);
        }
        if (tail.startsWith("/")) {
            tail = tail.substring(1);
        }
        return head + "/" + tail;
    }
}
