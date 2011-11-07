package org.ovirt.engine.core.utils.ovf;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.compat.backendcompat.XmlDocument;
import org.ovirt.engine.core.compat.backendcompat.XmlNamespaceManager;
import org.ovirt.engine.core.compat.backendcompat.XmlNode;
import org.ovirt.engine.core.compat.backendcompat.XmlNodeList;
import org.ovirt.engine.core.common.businessentities.*;

public class OvfParser {
    protected XmlDocument _document;
    protected XmlNamespaceManager _xmlNS;

    private static java.text.DateFormat utcDateTimeFormat;
    private static java.text.DateFormat utcDateTimeFormat2;

    static {
        utcDateTimeFormat = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        utcDateTimeFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

        utcDateTimeFormat2 = new java.text.SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        utcDateTimeFormat2.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
    }

    public OvfParser(String ovfstring) {
        _document = new XmlDocument();
        _document.LoadXml(ovfstring);

        _xmlNS = new XmlNamespaceManager(_document.NameTable);
    }

    public boolean IsTemplate() {
        String id1 = "1";
        String id2 = "2";

        XmlNode node = _document.SelectSingleNode("//*/Content/TemplateId");
        if (!StringHelper.isNullOrEmpty(node.InnerText)) {
            id1 = node.InnerText;
        }

        XmlNodeList list = _document.SelectNodes("//*/Content/Section");
        for (XmlNode section : list) {
            String value = section.Attributes.get("xsi:type").getValue();

            // C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a
            // string member and was converted to Java 'if-else' logic:
            // switch (value)
            // ORIGINAL LINE: case "ovf:OperatingSystemSection_Type":
            if (StringHelper.EqOp(value, "ovf:OperatingSystemSection_Type")) {
                id2 = section.Attributes.get("ovf:id").getValue();
            }
        }

        return StringHelper.EqOp(id1, id2);
    }

    // imageFile is: [image group id]/[image id]
    // 7D1FE0AA-A153-4AAF-95B3-3654A54443BE/7D1FE0AA-A153-4AAF-95B3-3654A54443BE
    public static String CreateImageFile(DiskImage image) {
        String retVal = "";
        if (image.getimage_group_id() != null) {
            retVal += image.getimage_group_id().getValue().toString();
        } else {
            retVal += Guid.Empty;
        }
        retVal += "/" + image.getId().toString();
        return retVal;
    }

    public static NGuid GetImageGrupIdFromImageFile(String imageFile) {
        if (!StringHelper.isNullOrEmpty(imageFile)) {
            return new Guid(imageFile.split("[/]", -1)[0]);
        }
        return null;
    }

    public static NGuid GetImageIdFromImageFile(String imageFile) {
        if (!StringHelper.isNullOrEmpty(imageFile)) {
            String[] all = imageFile.split("[/]", -1);
            if (all.length > 1) {
                return new Guid(imageFile.split("[/]", -1)[1]);
            }
        }
        return null;
    }

    public static String LocalDateToUtcDateString(java.util.Date date) {
        return utcDateTimeFormat.format(date);
    }

    /**
     * Method return false if the format is not yyyy/mm/dd hh:mm:ss
     *
     * @param str
     * @param date
     * @return
     */
    public static boolean UtcDateStringToLocaDate(String str, RefObject<java.util.Date> date) {
        date.argvalue = DateTime.getMinValue();
        if (StringHelper.isNullOrEmpty(str)) {
            return false;
        }

        try {
            date.argvalue = utcDateTimeFormat.parse(str);
            return true;
        } catch (java.text.ParseException e1) {
            try {
                date.argvalue = utcDateTimeFormat2.parse(str);
                return true;
            } catch (java.text.ParseException e) {
                log.error("OVF DateTime format Error, Expected: yyyy/M/dd hh:mm:ss", e);
                return false;
            }
        }
    }

    private static LogCompat log = LogFactoryCompat.getLog(OvfParser.class);
}
