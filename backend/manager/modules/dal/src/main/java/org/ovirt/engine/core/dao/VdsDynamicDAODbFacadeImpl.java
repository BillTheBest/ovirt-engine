package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.ovirt.engine.core.common.businessentities.HypervisorType;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsTransparentHugePagesState;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

/**
 * <code>VdsDAODbFacadeImpl</code> provides an implementation of {@link VdsDAO} that uses previously written code from
 * {@link org.ovirt.engine.core.dal.dbbroker.DbFacade}.
 *
 *
 */
public class VdsDynamicDAODbFacadeImpl extends BaseDAODbFacade implements VdsDynamicDAO {

    @Override
    public VdsDynamic get(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_id", id);

        ParameterizedRowMapper<VdsDynamic> mapper = new ParameterizedRowMapper<VdsDynamic>() {
            @Override
            public VdsDynamic mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                VdsDynamic entity = new VdsDynamic();
                entity.setcpu_cores((Integer) rs.getObject("cpu_cores"));
                entity.setcpu_model(rs.getString("cpu_model"));
                entity.setcpu_speed_mh(rs.getDouble("cpu_speed_mh"));
                entity.setif_total_speed(rs.getString("if_total_speed"));
                entity.setkvm_enabled((Boolean) rs.getObject("kvm_enabled"));
                entity.setmem_commited((Integer) rs.getObject("mem_commited"));
                entity.setphysical_mem_mb((Integer) rs
                        .getObject("physical_mem_mb"));
                entity.setstatus(VDSStatus.forValue(rs.getInt("status")));
                entity.setId(Guid.createGuidFromString(rs
                        .getString("vds_id")));
                entity.setvm_active((Integer) rs.getObject("vm_active"));
                entity.setvm_count((Integer) rs.getObject("vm_count"));
                entity.setvms_cores_count((Integer) rs
                        .getObject("vms_cores_count"));
                entity.setvm_migrating((Integer) rs.getObject("vm_migrating"));
                entity.setreserved_mem((Integer) rs.getObject("reserved_mem"));
                entity.setguest_overhead((Integer) rs
                        .getObject("guest_overhead"));
                entity.setsoftware_version(rs.getString("software_version"));
                entity.setversion_name(rs.getString("version_name"));
                entity.setbuild_name(rs.getString("build_name"));
                entity.setprevious_status(VDSStatus.forValue(rs
                        .getInt("previous_status")));
                entity.setcpu_flags(rs.getString("cpu_flags"));
                entity.setcpu_over_commit_time_stamp(DbFacadeUtils.fromDate(rs
                        .getTimestamp("cpu_over_commit_time_stamp")));
                entity.sethypervisor_type(HypervisorType.forValue(rs
                        .getInt("hypervisor_type")));
                entity.setpending_vcpus_count((Integer) rs
                        .getObject("pending_vcpus_count"));
                entity.setpending_vmem_size(rs.getInt("pending_vmem_size"));
                entity.setcpu_sockets((Integer) rs.getObject("cpu_sockets"));
                entity.setnet_config_dirty((Boolean) rs
                        .getObject("net_config_dirty"));
                entity.setsupported_cluster_levels(rs
                        .getString("supported_cluster_levels"));
                entity.setsupported_engines(rs.getString("supported_engines"));
                entity.sethost_os(rs.getString("host_os"));
                entity.setkvm_version(rs.getString("kvm_version"));
                entity.setspice_version(rs.getString("spice_version"));
                entity.setkernel_version(rs.getString("kernel_version"));
                entity.setIScsiInitiatorName(rs
                        .getString("iscsi_initiator_name"));
                entity.setTransparentHugePagesState(VdsTransparentHugePagesState
                        .forValue(rs.getInt("transparent_hugepages_state")));
                entity.setAnonymousHugePages(rs.getInt("anonymous_hugepages"));
                entity.setHooksStr(rs.getString("hooks"));
                entity.setNonOperationalReason(NonOperationalReason.forValue(rs
                        .getInt("non_operational_reason")));
                return entity;
            }
        };

        return getCallsHandler().executeRead("GetVdsDynamicByVdsId", mapper, parameterSource);
    }

    @Override
    public void save(VdsDynamic vds) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cpu_cores", vds.getcpu_cores())
                .addValue("cpu_model", vds.getcpu_model())
                .addValue("cpu_speed_mh", vds.getcpu_speed_mh())
                .addValue("if_total_speed", vds.getif_total_speed())
                .addValue("kvm_enabled", vds.getkvm_enabled())
                .addValue("mem_commited", vds.getmem_commited())
                .addValue("physical_mem_mb", vds.getphysical_mem_mb())
                .addValue("status", vds.getstatus())
                .addValue("vds_id", vds.getId())
                .addValue("vm_active", vds.getvm_active())
                .addValue("vm_count", vds.getvm_count())
                .addValue("vms_cores_count", vds.getvms_cores_count())
                .addValue("vm_migrating", vds.getvm_migrating())
                .addValue("reserved_mem", vds.getreserved_mem())
                .addValue("guest_overhead", vds.getguest_overhead())
                .addValue("software_version", vds.getsoftware_version())
                .addValue("version_name", vds.getversion_name())
                .addValue("build_name", vds.getbuild_name())
                .addValue("previous_status", vds.getprevious_status())
                .addValue("cpu_flags", vds.getcpu_flags())
                .addValue("cpu_over_commit_time_stamp",
                        vds.getcpu_over_commit_time_stamp())
                .addValue("hypervisor_type", vds.gethypervisor_type())
                .addValue("pending_vcpus_count", vds.getpending_vcpus_count())
                .addValue("pending_vmem_size", vds.getpending_vmem_size())
                .addValue("cpu_sockets", vds.getcpu_sockets())
                .addValue("net_config_dirty", vds.getnet_config_dirty())
                .addValue("supported_cluster_levels",
                        vds.getsupported_cluster_levels())
                .addValue("supported_engines", vds.getsupported_engines())
                .addValue("host_os", vds.gethost_os())
                .addValue("kvm_version", vds.getkvm_version())
                .addValue("spice_version", vds.getspice_version())
                .addValue("kernel_version", vds.getkernel_version())
                .addValue("iscsi_initiator_name", vds.getIScsiInitiatorName())
                .addValue("transparent_hugepages_state",
                        vds.getTransparentHugePagesState().getValue())
                .addValue("anonymous_hugepages", vds.getAnonymousHugePages())
                .addValue("hooks", vds.getHooksStr())
                .addValue("non_operational_reason",
                        vds.getNonOperationalReason().getValue());

        getCallsHandler().executeModification("InsertVdsDynamic", parameterSource);
    }

    @Override
    public void update(VdsDynamic vds) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cpu_cores", vds.getcpu_cores())
                .addValue("cpu_model", vds.getcpu_model())
                .addValue("cpu_speed_mh", vds.getcpu_speed_mh())
                .addValue("if_total_speed", vds.getif_total_speed())
                .addValue("kvm_enabled", vds.getkvm_enabled())
                .addValue("mem_commited", vds.getmem_commited())
                .addValue("physical_mem_mb", vds.getphysical_mem_mb())
                .addValue("status", vds.getstatus())
                .addValue("vds_id", vds.getId())
                .addValue("vm_active", vds.getvm_active())
                .addValue("vm_count", vds.getvm_count())
                .addValue("vms_cores_count", vds.getvms_cores_count())
                .addValue("vm_migrating", vds.getvm_migrating())
                .addValue("reserved_mem", vds.getreserved_mem())
                .addValue("guest_overhead", vds.getguest_overhead())
                .addValue("software_version", vds.getsoftware_version())
                .addValue("version_name", vds.getversion_name())
                .addValue("build_name", vds.getbuild_name())
                .addValue("previous_status", vds.getprevious_status())
                .addValue("cpu_flags", vds.getcpu_flags())
                .addValue("cpu_over_commit_time_stamp",
                        vds.getcpu_over_commit_time_stamp())
                .addValue("hypervisor_type", vds.gethypervisor_type())
                .addValue("pending_vcpus_count", vds.getpending_vcpus_count())
                .addValue("pending_vmem_size", vds.getpending_vmem_size())
                .addValue("cpu_sockets", vds.getcpu_sockets())
                .addValue("net_config_dirty", vds.getnet_config_dirty())
                .addValue("supported_cluster_levels",
                        vds.getsupported_cluster_levels())
                .addValue("supported_engines", vds.getsupported_engines())
                .addValue("host_os", vds.gethost_os())
                .addValue("kvm_version", vds.getkvm_version())
                .addValue("spice_version", vds.getspice_version())
                .addValue("kernel_version", vds.getkernel_version())
                .addValue("iscsi_initiator_name", vds.getIScsiInitiatorName())
                .addValue("transparent_hugepages_state",
                        vds.getTransparentHugePagesState().getValue())
                .addValue("anonymous_hugepages", vds.getAnonymousHugePages())
                .addValue("hooks", vds.getHooksStr())
                .addValue("non_operational_reason",
                        vds.getNonOperationalReason().getValue());

        getCallsHandler().executeModification("UpdateVdsDynamic", parameterSource);
    }

    @Override
    public void remove(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_id", id);

        getCallsHandler().executeModification("DeleteVdsDynamic", parameterSource);
    }

    @Override
    public List<VdsDynamic> getAll() {
        throw new NotImplementedException();
    }

    @Override
    public void updateStatus(Guid id, VDSStatus status) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_guid", id)
                .addValue("status", status);

        getCallsHandler().executeModification("UpdateVdsDynamicStatus", parameterSource);
    }
}
