package com.inmobi.template.formatter;

import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inmobi.template.exception.TemplateException;

/**
 * 
 * @author ritwik.kumar
 *
 */
public class TemplateDecorator {
    private final static Logger LOG = LoggerFactory.getLogger(TemplateDecorator.class);
    private String contextCodeFile;

    @Inject
    public void addContextFile(@Named("ContextCodeFile") final String contextCodeVm) throws TemplateException {
        contextCodeFile = contextCodeVm;
        final String cc = getFileContent(contextCodeFile);
        TemplateManager.getInstance().addToTemplateCache(contextCodeFile, cc);
    }

    /**
     * 
     * @param velocityContext
     * @return
     * @throws TemplateException
     */
    public String getContextCode(final VelocityContext velocityContext) throws TemplateException {
        return getTemplateContent(velocityContext, contextCodeFile);
    }

    /**
     * 
     * @param fileName
     * @return
     * @throws TemplateException
     */
    private String getFileContent(final String fileName) throws TemplateException {
        try {
            final InputStream is = TemplateDecorator.class.getResourceAsStream(fileName);
            return IOUtils.toString(is);
        } catch (final Exception e) {
            throw new TemplateException("Error while reading resource", e);
        }

    }

    /**
     * 
     * @param velocityContext
     * @param templateName
     * @return
     * @throws TemplateException
     */
    private String getTemplateContent(final VelocityContext velocityContext, final String templateName)
            throws TemplateException {
        try {
            final Template template = TemplateManager.getInstance().getTemplate(templateName);
            final StringWriter writer = new StringWriter();
            template.merge(velocityContext, writer);
            return writer.toString();
        } catch (final Exception e) {
            LOG.error(String.format("Error while fetching context code for %s", templateName));
            throw new TemplateException("Exception occured while fetching context code", e);
        }
    }


}
