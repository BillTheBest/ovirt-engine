package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

/**
 * <code>NetworkStatistics</code>
 *
 */
@XmlSeeAlso({ VmNetworkStatistics.class, VdsNetworkStatistics.class })
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "NetworkStatistics")
public abstract class NetworkStatistics implements BusinessEntity<Guid> {
    private static final long serialVersionUID = -748737255583275169L;

    @XmlElement(name = "Id")
    private Guid id;

    @XmlElement(name = "Status")
    private InterfaceStatus status;

    @XmlElement(name = "ReceiveDropRate", nillable = true)
    private Double receiveDropRate;

    @XmlElement(name = "ReceiveRate", nillable = true)
    private Double receiveRate;

    @XmlElement(name = "TransmitDropRate", nillable = true)
    private Double transmitDropRate;

    @XmlElement(name = "TransmitRate", nillable = true)
    private Double transmitRate;

    /**
     * Sets the instance id.
     *
     * @param id
     *            the id
     */
    public void setId(Guid id) {
        this.id = id;
    }

    /**
     * Returns the instance id.
     *
     * @return the id.
     */
    public Guid getId() {
        return id;
    }

    /**
     * Sets the status for the connection.
     *
     * @param status
     *            the status
     */
    public void setStatus(InterfaceStatus status) {
        this.status = status;
    }

    /**
     * Returns the connection status.
     *
     * @return the status
     */
    public InterfaceStatus getStatus() {
        return status;
    }

    /**
     * Sets the received data drop rate.
     *
     * @param receiveDropRate
     *            the rate
     */
    public void setReceiveDropRate(Double receiveDropRate) {
        this.receiveDropRate = receiveDropRate;
    }

    /**
     * Returns the received data drop rate.
     *
     * @return the rate
     */
    public Double getReceiveDropRate() {
        return receiveDropRate;
    }

    /**
     * Sets the data receive rate.
     *
     * @param receiveRate
     *            the rate
     */
    public void setReceiveRate(Double receiveRate) {
        this.receiveRate = receiveRate;
    }

    /**
     * Returns the data receive rate.
     *
     * @return the rate
     */
    public Double getReceiveRate() {
        return receiveRate;
    }

    /**
     * Sets the transmitted data drop rate.
     *
     * @param transmitDropRate
     *            the rate
     */
    public void setTransmitDropRate(Double transmitDropRate) {
        this.transmitDropRate = transmitDropRate;
    }

    /**
     * Returns the transmitted data drop rate.
     *
     * @return the rate
     */
    public Double getTransmitDropRate() {
        return transmitDropRate;
    }

    /**
     * Sets the data transmit rate.
     *
     * @param transmitRate
     *            the rate
     */
    public void setTransmitRate(Double transmitRate) {
        this.transmitRate = transmitRate;
    }

    /**
     * Returns the data transmit rate.
     *
     * @return the rate
     */
    public Double getTransmitRate() {
        return transmitRate;
    }
}
