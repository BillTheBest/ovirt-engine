package org.ovirt.engine.ui.webadmin;

import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;

public interface ApplicationTemplates extends SafeHtmlTemplates {

    /**
     * Creates a progress bar template.
     * 
     * @param progress
     *            Progress value in percent.
     * @param text
     *            Text to show within the progress bar.
     */
    @Template("<div class='engine-progress-box'>" +
            "<div style='background: {2}; width: {0}%; height: 100%'></div>" +
            "<div class='engine-progress-text'>{1}</div>" +
            "</div>")
    SafeHtml progressBar(int progress, String text, String color);

    /**
     * Creates a tree-item HTML
     * 
     * @param imageHtml
     *            the image HTML
     * @param text
     *            the node title
     * @return
     */
    @Template("<span style='position: relative; bottom: 1px;'>{0}</span><span style='position: relative; bottom: 7px;'>{1}</span>")
    SafeHtml treeItem(SafeHtml imageHtml, String text);

    /**
     * Creates a bookmark-item HTML
     * 
     * @param text
     *            the bookmark title
     */
    @Template("<span style='display: inline-block; padding: 5px;'>{0}</span>")
    SafeHtml bookmarkItem(String text);

    /**
     * Creates a tag-item HTML
     * 
     * @param imageHtml
     *            the image HTML
     * @param text
     *            the node title
     * @return
     */
    @Template("<span style='position: relative; border: 1px solid {3}; " +
            "bottom: 4px; padding: 0 3px; margin: 0 1px; background-color: {2};'>" +
            "<span style='position: relative; top: 1px;'>{0}</span> {1}</span>")
    SafeHtml tagItem(SafeHtml imageHtml, String text, String backgroundColor, String borderColor);

    /**
     * Creates a tag-button HTML
     * 
     * @param imageHtml
     *            the image HTML
     * @return
     */
    @Template("<span style='position: relative; border: 1px solid {2}; visibility: {3};" +
            " bottom: 4px; padding: 0 3px; background-color: {1};'>{0}</span>")
    SafeHtml tagButton(SafeHtml imageHtml, String backgroundColor, String borderColor, String visibility);

}
