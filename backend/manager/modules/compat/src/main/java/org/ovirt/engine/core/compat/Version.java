package org.ovirt.engine.core.compat;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
@XmlSeeAlso({
        Version.class,
        RpmVersion.class })
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "version")
public class Version implements Comparable<Version>, Serializable {
    private static final long serialVersionUID = -3938214651005908651L;

    private int major;
    private int minor;
    private int build;
    private int revision;

    public Version(String value) {
        this();
        setValue(value);
    }

    public Version() {
        major = minor = build = revision = -1;
    }

    public Version(int major, int minor) {
        this.major = major;
        this.minor = minor;
        build = revision = -1;
    }

    public Version(int major2, int minor2, int build2) {
        this.major = major2;
        this.minor = minor2;
        this.build = build2;
        this.revision = -1;
    }

    public Version(int major2, int minor2, int build2, Integer revision2) {
        this.major = major2;
        this.minor = minor2;
        this.build = build2;
        this.revision = revision2;
    }

    @XmlElement(name = "value")
    public String getValue() {
        String val = "";

        if (this.major > -1)
            val += this.major;
        if (this.minor > -1)
            val += (val != "" ? "." : "") + this.minor;
        if (this.build > -1)
            val += (val != "" ? "." : "") + this.build;
        if (this.revision > -1)
            val += (val != "" ? "." : "") + this.revision;

        return val;
    }

    public void setValue(String value) {
        if (value == null || value.isEmpty()) {
            major = minor = build = revision = -1;
        } else {
            String[] partialVersions = value.split("\\.");
            switch (partialVersions.length) {
            case (4):
                revision = Integer.parseInt(partialVersions[3]);
            case (3):
                build = Integer.parseInt(partialVersions[2]);
            case (2):
                minor = Integer.parseInt(partialVersions[1]);
            case (1):
                major = Integer.parseInt(partialVersions[0]);
            }
        }
    }

    public String toString(int i) {
        StringBuilder sb = new StringBuilder();
        switch (i) {
        case (4):
            sb.append(".").append(revision);
        case (3):
            sb.insert(0, build).insert(0, ".");
        case (2):
            sb.insert(0, minor).insert(0, ".");
        case (1):
            sb.insert(0, major);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return this.getValue();
    }

    @Override
    public int compareTo(Version other) {
        if (other == null)
            return 5;
        if (this == other)
            return 0;
        int result = major - other.major;
        if (result == 0) {
            result = minor - other.minor;
            if (result == 0) {
                result = build - other.build;
                if (result == 0) {
                    result = revision - other.revision;
                }
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + major;
        result = prime * result + minor;
        result = prime * result + revision;
        result = prime * result + build;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        Version other = (Version) obj;
        if (major == other.major && minor == other.minor && revision == other.revision && build == other.build) {
            return true;
        }
        return false;

    }

    public static boolean OpEquality(Version v1, Version v2) {
        if (v1 == null)
            return (v2 == null) ? true : false;
        else
            return v1.equals(v2);
    }

    public static boolean OpInequality(Version v1, Version v2) {
        return !OpEquality(v1, v2);
    }

    public int getMajor() {
        return this.major;
    }

    public int getMinor() {
        return this.minor;
    }

    public int getBuild() {
        return this.build;
    }

    public int getRevision() {
        return this.revision;
    }

}
