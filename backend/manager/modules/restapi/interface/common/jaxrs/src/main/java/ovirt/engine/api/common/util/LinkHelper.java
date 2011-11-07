/*
* Copyright © 2010 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*           http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.common.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.ovirt.engine.api.model.ActionsBuilder;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.CdRom;
import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.model.Event;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.HostNIC;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.File;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.model.Link;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.Permission;
import org.ovirt.engine.api.model.Permit;
import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.Storage;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.Tag;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.model.VmPool;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.resource.AssignedNetworkResource;
import org.ovirt.engine.api.resource.AssignedNetworksResource;
import org.ovirt.engine.api.resource.AssignedTagResource;
import org.ovirt.engine.api.resource.AssignedTagsResource;
import org.ovirt.engine.api.resource.AttachedStorageDomainResource;
import org.ovirt.engine.api.resource.AttachedStorageDomainsResource;
import org.ovirt.engine.api.resource.ClusterResource;
import org.ovirt.engine.api.resource.ClustersResource;
import org.ovirt.engine.api.resource.DataCenterResource;
import org.ovirt.engine.api.resource.DataCentersResource;
import org.ovirt.engine.api.resource.DeviceResource;
import org.ovirt.engine.api.resource.DevicesResource;
import org.ovirt.engine.api.resource.DomainsResource;
import org.ovirt.engine.api.resource.DomainResource;
import org.ovirt.engine.api.resource.DiskResource;
import org.ovirt.engine.api.resource.EventResource;
import org.ovirt.engine.api.resource.EventsResource;
import org.ovirt.engine.api.resource.ReadOnlyDeviceResource;
import org.ovirt.engine.api.resource.ReadOnlyDevicesResource;
import org.ovirt.engine.api.resource.RolesResource;
import org.ovirt.engine.api.resource.HostResource;
import org.ovirt.engine.api.resource.HostsResource;
import org.ovirt.engine.api.resource.HostStorageResource;
import org.ovirt.engine.api.resource.HostNicResource;
import org.ovirt.engine.api.resource.HostNicsResource;
import org.ovirt.engine.api.resource.FileResource;
import org.ovirt.engine.api.resource.FilesResource;
import org.ovirt.engine.api.resource.GroupResource;
import org.ovirt.engine.api.resource.GroupsResource;
import org.ovirt.engine.api.resource.NetworkResource;
import org.ovirt.engine.api.resource.NetworksResource;
import org.ovirt.engine.api.resource.NicResource;
import org.ovirt.engine.api.resource.PermissionResource;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.PermitResource;
import org.ovirt.engine.api.resource.PermitsResource;
import org.ovirt.engine.api.resource.RoleResource;
import org.ovirt.engine.api.resource.AssignedRolesResource;
import org.ovirt.engine.api.resource.SnapshotResource;
import org.ovirt.engine.api.resource.SnapshotsResource;
import org.ovirt.engine.api.resource.StatisticResource;
import org.ovirt.engine.api.resource.StatisticsResource;
import org.ovirt.engine.api.resource.StorageResource;
import org.ovirt.engine.api.resource.StorageDomainContentResource;
import org.ovirt.engine.api.resource.StorageDomainContentsResource;
import org.ovirt.engine.api.resource.StorageDomainResource;
import org.ovirt.engine.api.resource.StorageDomainsResource;
import org.ovirt.engine.api.resource.TagResource;
import org.ovirt.engine.api.resource.TagsResource;
import org.ovirt.engine.api.resource.TemplateResource;
import org.ovirt.engine.api.resource.TemplatesResource;
import org.ovirt.engine.api.resource.UserResource;
import org.ovirt.engine.api.resource.UsersResource;
import org.ovirt.engine.api.resource.VmPoolResource;
import org.ovirt.engine.api.resource.VmPoolsResource;
import org.ovirt.engine.api.resource.VmResource;
import org.ovirt.engine.api.resource.VmsResource;
import org.ovirt.engine.api.resource.DomainGroupResource;
import org.ovirt.engine.api.resource.DomainGroupsResource;
import org.ovirt.engine.api.resource.DomainUserResource;
import org.ovirt.engine.api.resource.DomainUsersResource;

/**
 * Contains a static addLinks() method which constructs any href attributes
 * and action links required by a representation.
 *
 * The information used to build links is obtained from the annotations on
 * the API definition interfaces.

 * For example, a link to a VM is the combination of the @Path attribute on
 * VmsResource and the VM id - i.e. '/restapi-definition/vms/{vm_id}'
 *
 * Resource collections which are a sub-resource of a parent collection
 * present a more difficult challenge. For example, the link to a VM tag
 * is the combination of the @Path attribute on VmsResource, the VM id,
 * the @Path attribute on VmResource.getTagsResource() and the tag id -
 * i.e. '/restapi-definition/vms/{vm_id}/tags/{tag_id}'
 * In most cases the parent type may be computed, but in exceptional
 * cases there are a number of equally valid candidates. Disambiguation
 * is achieved via an explicit suggestedParentType parameter.
 *
 * To be able to do this we need, for each collection, the collection type
 * (e.g. AssignedTagsResource), the resource type (e.g. AssignedTagResource)
 * and the parent model type (e.g. VM). The TYPES map below is populated
 * with this information for every resource type.
 */
public class LinkHelper {

    private static final String SEARCH_RELATION = "/search";
    private static final String SEARCH_TEMPLATE = "?search={query}";
    private static final String PARAMETER_TEMPLATE = "&%s={%s}";

    /**
     * A constant representing the pseudo-parent of a top-level collection
     */
    private static final Class<? extends BaseResource> NO_PARENT = BaseResource.class;

    /**
     * A map describing every possible collection
     */
    private static ModelToCollectionsMap TYPES = new ModelToCollectionsMap();

    static {
        ParentToCollectionMap map;

        map = new ParentToCollectionMap(ReadOnlyDeviceResource.class, ReadOnlyDevicesResource.class, Template.class);
        TYPES.put(CdRom.class, map);

        map = new ParentToCollectionMap(DeviceResource.class, DevicesResource.class, VM.class);
        TYPES.put(CdRom.class, map);

        map = new ParentToCollectionMap(ClusterResource.class, ClustersResource.class);
        TYPES.put(Cluster.class, map);

        map = new ParentToCollectionMap(DataCenterResource.class, DataCentersResource.class);
        TYPES.put(DataCenter.class, map);

        map = new ParentToCollectionMap(ReadOnlyDeviceResource.class, ReadOnlyDevicesResource.class, Template.class);
        TYPES.put(Disk.class, map);

        map = new ParentToCollectionMap(DeviceResource.class, DevicesResource.class, VM.class);
        TYPES.put(Disk.class, map);

        map = new ParentToCollectionMap(DiskResource.class, DevicesResource.class, VM.class);
        TYPES.put(Disk.class, map);

        map = new ParentToCollectionMap(HostResource.class, HostsResource.class);
        TYPES.put(Host.class, map);

        map = new ParentToCollectionMap(HostNicResource.class, HostNicsResource.class, Host.class);
        TYPES.put(HostNIC.class, map);

        map = new ParentToCollectionMap(FileResource.class, FilesResource.class, StorageDomain.class);
        TYPES.put(File.class, map);

        map = new ParentToCollectionMap(GroupResource.class, GroupsResource.class);
        map.add(DomainGroupResource.class, DomainGroupsResource.class, Domain.class);
        TYPES.put(Group.class, map);

        map = new ParentToCollectionMap(PermissionResource.class, AssignedPermissionsResource.class, User.class);
        map.add(PermissionResource.class, AssignedPermissionsResource.class, Group.class);
        map.add(PermissionResource.class, AssignedPermissionsResource.class, Role.class);
        map.add(PermissionResource.class, AssignedPermissionsResource.class, VM.class);
        TYPES.put(Permission.class, map);

        map = new ParentToCollectionMap(NetworkResource.class, NetworksResource.class);
        map.add(AssignedNetworkResource.class, AssignedNetworksResource.class, Cluster.class);
        TYPES.put(Network.class, map);

        map = new ParentToCollectionMap(DeviceResource.class, DevicesResource.class);
        map.add(DeviceResource.class, DevicesResource.class, VM.class);
        map.add(DeviceResource.class, DevicesResource.class, Template.class);
        map.add(ReadOnlyDeviceResource.class, ReadOnlyDevicesResource.class, Template.class);
        map.add(NicResource.class, DevicesResource.class, VM.class);
        TYPES.put(NIC.class, map);

        map = new ParentToCollectionMap(PermitResource.class, PermitsResource.class, Role.class);
        TYPES.put(Permit.class, map);

        map = new ParentToCollectionMap(RoleResource.class, RolesResource.class);
        map.add(RoleResource.class, AssignedRolesResource.class, User.class);
        TYPES.put(Role.class, map);

        map = new ParentToCollectionMap(SnapshotResource.class, SnapshotsResource.class, VM.class);
        TYPES.put(Snapshot.class, map);

        map = new ParentToCollectionMap(StorageResource.class, HostStorageResource.class, Host.class);
        TYPES.put(Storage.class, map);

        map = new ParentToCollectionMap(StorageDomainResource.class, StorageDomainsResource.class);
        map.add(AttachedStorageDomainResource.class, AttachedStorageDomainsResource.class, DataCenter.class);
        TYPES.put(StorageDomain.class, map);

        map = new ParentToCollectionMap(TagResource.class, TagsResource.class);
        map.add(AssignedTagResource.class, AssignedTagsResource.class, Host.class);
        map.add(AssignedTagResource.class, AssignedTagsResource.class, User.class);
        map.add(AssignedTagResource.class, AssignedTagsResource.class, VM.class);
        map.add(AssignedTagResource.class, AssignedTagsResource.class, Group.class);
        TYPES.put(Tag.class, map);

        map = new ParentToCollectionMap(TemplateResource.class, TemplatesResource.class);
        map.add(StorageDomainContentResource.class, StorageDomainContentsResource.class, StorageDomain.class);
        TYPES.put(Template.class, map);

        map = new ParentToCollectionMap(UserResource.class, UsersResource.class);
        map.add(DomainUserResource.class, DomainUsersResource.class, Domain.class);
        TYPES.put(User.class, map);

        map = new ParentToCollectionMap(VmResource.class, VmsResource.class);
        map.add(StorageDomainContentResource.class, StorageDomainContentsResource.class, StorageDomain.class);
        TYPES.put(VM.class, map);

        map = new ParentToCollectionMap(VmPoolResource.class, VmPoolsResource.class);
        TYPES.put(VmPool.class, map);

        map = new ParentToCollectionMap(EventResource.class, EventsResource.class);
        TYPES.put(Event.class, map);

        map = new ParentToCollectionMap(DomainResource.class, DomainsResource.class);
        TYPES.put(Domain.class, map);

        map = new ParentToCollectionMap(StatisticResource.class, StatisticsResource.class, Disk.class);
        map.add(StatisticResource.class, StatisticsResource.class, Host.class);
        map.add(StatisticResource.class, StatisticsResource.class, HostNIC.class);
        map.add(StatisticResource.class, StatisticsResource.class, NIC.class);
        map.add(StatisticResource.class, StatisticsResource.class, VM.class);
        TYPES.put(Statistic.class, map);
    }

    /**
     * Obtain the relative path to a top-level collection
     *
     * The path is simply the value of the @Path annotation on the
     * supplied collection resource type
     *
     * @param clz the collection resource type
     * @return    the relative path to the collection
     */
    private static String getPath(Class<?> clz) {
        Path pathAnnotation = (Path)clz.getAnnotation(Path.class);

        String path = pathAnnotation.value();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        return path;
    }

    /**
     * Obtain the relative path to a sub-collection
     *
     * The path is obtained from the @Path annotation on the method on @parent
     * which returns an instance of @clz
     *
     * A case-insensitive check for @type's name as a substring of the method
     * is also performed to guard against the case where @parent has multiple
     * methods returning instances of @clz, e.g. VmResource has multiple
     * methods return DevicesResource instances
     *
     * @param clz    the collection resource type (e.g. AssignedTagsResource)
     * @param parent the parent resource type (e.g. VmResource)
     * @param type   the model type (e.g. Tag)
     * @return       the relative path to the collection
     */
    private static String getPath(Class<?> clz, Class<?> parent, Class<?> type) {
        for (Method method : parent.getMethods()) {
            if (method.getName().startsWith("get") &&
                clz.isAssignableFrom(method.getReturnType()) &&
                isPluralResourceGetter(method.getName(), type.getSimpleName())) {
                Path pathAnnotation = (Path)method.getAnnotation(Path.class);
                return pathAnnotation.value();
            }
        }
        return null;
    }

    private static boolean isPluralResourceGetter(String method, String type) {
        method = method.toLowerCase();
        type = type.toLowerCase();

        method = chopStart(method, "get");
        method = chopEnd(method, "resource");
        method = chopEnd(method, "s");

        if (type.endsWith("y")) {
            method = chopEnd(method, "ie");
            type = chopEnd(type, "y");
        }

        return method.contains(type);
    }

    private static String chopStart(String str, String chop) {
        if (str.startsWith(chop)) {
            return str.substring(chop.length());
        } else {
            return str;
        }
    }

    private static String chopEnd(String str, String chop) {
        if (str.endsWith(chop)) {
            return str.substring(0, str.length() - chop.length());
        } else {
            return str;
        }
    }

    /**
     * Obtain a set of inline BaseResource objects from @obj
     *
     * i.e. return the value of any properties on @obj which are a
     * sub-type of BaseResource
     *
     * @param obj the object to check
     * @return    a list of any inline BaseResource objects
     */
    private static List<BaseResource> getInlineResources(Object obj) {
        ArrayList<BaseResource> ret = new ArrayList<BaseResource>();

        for (Method method : obj.getClass().getMethods()) {
            if (method.getName().startsWith("get") &&
                BaseResource.class.isAssignableFrom(method.getReturnType())) {
                try {
                    BaseResource inline = (BaseResource)method.invoke(obj);
                    if (inline != null) {
                        ret.add(inline);
                    }
                } catch (Exception e) {
                    // invocation target exception should not occur on simple getter
                }
            }
        }

        return ret;
    }

    /**
     * Unset the property on @model of type @type
     *
     * @param model the object with the property to unset
     * @param type  the type of the property
     */
    private static void unsetInlineResource(BaseResource model, Class<?> type) {
        for (Method method : model.getClass().getMethods()) {
            if (method.getName().startsWith("set")) {
                try {
                    if (type.isAssignableFrom(method.getParameterTypes()[0])) {
                        method.invoke(model, new Object[]{null});
                        return;
                    }
                } catch (Exception e) {
                    // invocation target exception should not occur on simple setter
                }
            }
        }
    }

    /**
     * Return any parent object set on @model
     *
     * i.e. return the value of any bean property whose type matches @parentType
     *
     * @param model      object to check
     * @param parentType the type of the parent
     * @return           the parent object, or null if not set
     */
    private static <R extends BaseResource> BaseResource getParentModel(R model, Class<?> parentType) {
        for (BaseResource inline : getInlineResources(model)) {
            if (parentType.isAssignableFrom(inline.getClass())) {
                return inline;
            }
        }
        return null;
    }

    /**
     * Lookup the #Collection instance which represents this object
     *
     * i.e. for a VM tag (i.e. a Tag object which its VM property set)
     * return the #Collection instance which encapsulates AssignedTagResource,
     * AssignedTagsResource and VM.
     *
     * @param model the object to query for
     * @return      the #Collection instance representing the object's collection
     */
    private static Collection getCollection(BaseResource model) {
        return getCollection(model, null);
    }

    /**
     * Lookup the #Collection instance which represents this object
     *
     * i.e. for a VM tag (i.e. a Tag object which its VM property set)
     * return the #Collection instance which encapsulates AssignedTagResource,
     * AssignedTagsResource and VM.
     *
     * @param model                the object to query for
     * @param suggestedParentType  the suggested parent type
     * @return                     the #Collection instance representing the object's collection
     */
    private static Collection getCollection(BaseResource model, Class<? extends BaseResource> suggestedParentType) {
        ParentToCollectionMap collections = TYPES.get(model.getClass());

        if (suggestedParentType != null) {
            for (Class<? extends BaseResource> parentType : collections.keySet()) {
                if (parentType.equals(suggestedParentType)) {
                    return collections.get(parentType);
                }
            }
        }

        for (Class<? extends BaseResource> parentType : collections.keySet()) {
            if (parentType != NO_PARENT &&
                getParentModel(model, parentType) != null) {
                return collections.get(parentType);
            }
        }

        return collections.get(NO_PARENT);
    }

    /**
     * Create a #UriBuilder which encapsulates the path to an object
     *
     * i.e. for a VM tag, return a UriBuilder which encapsulates
     * '/restapi-definition/vms/{vm_id}/tags/{tag_id}'
     *
     * @param uriInfo the URI info
     * @param model   the object
     * @return        the #UriBuilder encapsulating the object's path
     */
    public static <R extends BaseResource> UriBuilder getUriBuilder(UriInfo uriInfo, R model) {
        return getUriBuilder(uriInfo, model, null);
    }

    /**
     * Create a #UriBuilder which encapsulates the path to an object
     *
     * i.e. for a VM tag, return a UriBuilder which encapsulates
     * '/restapi-definition/vms/{vm_id}/tags/{tag_id}'
     *
     * @param uriInfo              the URI info
     * @param model                the object
     * @param suggestedParentType  the suggested parent type
     * @return                     the #UriBuilder encapsulating the object's path
     */
    public static <R extends BaseResource> UriBuilder getUriBuilder(UriInfo uriInfo, R model, Class<? extends BaseResource> suggestedParentType) {
        Collection collection = getCollection(model, suggestedParentType);
        if (collection == null) {
            return null;
        }

        UriBuilder uriBuilder;

        if (collection.getParentType() != NO_PARENT) {
            BaseResource parent = getParentModel(model, collection.getParentType());

            Collection parentCollection = getCollection(parent, suggestedParentType);

            String path = getPath(collection.getCollectionType(),
                                  parentCollection.getResourceType(),
                                  model.getClass());

            uriBuilder = getUriBuilder(uriInfo, parent).path(path);
        } else {
            String path = getPath(collection.getCollectionType());
            uriBuilder = uriInfo != null
                         ? UriBuilder.fromPath(uriInfo.getBaseUri().getPath()).path(path)
                         : UriBuilder.fromPath(path);
        }

        return uriBuilder.path(model.getId());
    }

    /**
     * Set the href attribute on the supplied object
     *
     * e.g. set href = '/restapi-definition/vms/{vm_id}/tags/{tag_id}' on a VM tag
     *
     * @param uriInfo  the URI info
     * @param model    the object
     * @return         the model, with the href attribute set
     */
    private static <R extends BaseResource> void setHref(UriInfo uriInfo, R model) {
        setHref(uriInfo, model, null);
    }

    /**
     * Set the href attribute on the supplied object
     *
     * e.g. set href = '/restapi-definition/vms/{vm_id}/tags/{tag_id}' on a VM tag
     *
     * @param uriInfo              the URI info
     * @param model                the object
     * @param suggestedParentType  the suggested parent type
     * @return                     the model, with the href attribute set
     */
    private static <R extends BaseResource> void setHref(UriInfo uriInfo, R model, Class<? extends BaseResource> suggestedParentType) {
        UriBuilder uriBuilder = getUriBuilder(uriInfo, model, suggestedParentType);
        if (uriBuilder != null) {
            model.setHref(uriBuilder.build().toString());
        }
    }

    /**
     * Construct the set of action links for an object
     *
     * @param uriInfo the URI info
     * @param model   the object
     * @param suggestedParentType  the suggested parent type
     * @return        the object, including its set of action links
     */
    private static <R extends BaseResource> void setActions(UriInfo uriInfo, R model, Class<? extends BaseResource> suggestedParentType) {
        Collection collection = getCollection(model);
        UriBuilder uriBuilder = getUriBuilder(uriInfo, model, suggestedParentType);
        if (uriBuilder != null) {
            ActionsBuilder actionsBuilder = new ActionsBuilder(uriBuilder, collection.getResourceType());
            model.setActions(actionsBuilder.build());
        }
    }

    /**
     * Set the href attribute on the object (and its inline objects)
     * and construct its set of action links
     *
     * @param uriInfo  the URI info
     * @param model    the object
     * @param suggestedParentType  the suggested parent type
     * @return         the object, with href attributes and action links
     */
    public static <R extends BaseResource> R addLinks(UriInfo uriInfo, R model) {
        return addLinks(uriInfo, model, null);
    }

    public static <R extends BaseResource> R addLinks(UriInfo uriInfo, R model, Class<? extends BaseResource> suggestedParentType) {
        setHref(uriInfo, model, suggestedParentType);
        setActions(uriInfo, model, suggestedParentType);

        for (BaseResource inline : getInlineResources(model)) {
            if (inline.getId() != null) {
                setHref(uriInfo, inline);
            }
            for (BaseResource grandParent : getInlineResources(inline)) {
                unsetInlineResource(inline, grandParent.getClass());
            }
        }

        return model;
    }

    /**
     * Appends searchable links to resource's Href
     *
     * @param url to append to
     * @param resource to add links to
     * @param rel link ro add
     * @param flags used to specify different link options
     */
    public static void addLink(BaseResource resource, String rel, LinkFlags flags) {
        addLink(resource.getHref(), resource, rel, flags);
    }

    /**
     * Adds searchable links to resource
     *
     * @param url to append to
     * @param resource to add links to
     * @param rel link ro add
     * @param flags used to specify different link options
     */
    public static void addLink(String url, BaseResource resource, String rel, LinkFlags flags) {
        addLink(url, resource, rel, flags, new HashMap<String, String>());
    }

    /**
     * Adds searchable links to resource
     *
     * @param url to append to
     * @param resource to add links to
     * @param rel link to add
     * @param flags used to specify different link options
     * @param params the URL params to append
     */
    public static void addLink(String url, BaseResource resource, String rel, LinkFlags flags, Map<String, String> params) {
        Link link = new Link();
        link.setRel(rel);
        link.setHref(combine(url, rel));
        resource.getLinks().add(link);

        if (flags == LinkFlags.SEARCHABLE) {
            addLink(url, resource, rel, params);
        }
    }

    /**
     * Appends searchable links to resource's Href
     *
     * @param url to append to and combine search dialect
     * @param resource to add links to
     * @param rel link ro add
     */
    public static void addLink(BaseResource resource, String rel) {
        addLink(resource.getHref(), resource, rel);
    }

    /**
     * Adds searchable links to resource
     *
     * @param url to append to and combine search dialect
     * @param resource to add links to
     * @param rel link ro add
     * @param params the URL params to append
     */
    public static void addLink(String url, BaseResource resource, String rel, Map<String, String> params) {
        Link link = new Link();
        link.setRel(rel + SEARCH_RELATION);
        link.setHref(combine(combine(url, rel) + SEARCH_TEMPLATE, params));
        resource.getLinks().add(link);
    }

    /**
     * Adds searchable links to resource
     *
     * @param url to append to and combine search dialect
     * @param resource to add links to
     * @param rel link ro add
     */
    public static void addLink(String url, BaseResource resource, String rel) {
        Link link = new Link();
        link.setRel(rel + SEARCH_RELATION);
        link.setHref(combine(url, rel) + SEARCH_TEMPLATE);
        resource.getLinks().add(link);
    }

    /**
     * Combine head and tail portions of a URI path.
     *
     * @param head the path head
     * @param tail the path tail
     * @return the combined head and tail
     */
    public static String combine(String head, String tail) {
        if (head.endsWith("/")) {
            head = head.substring(0, head.length() - 1);
        }
        if (tail.startsWith("/")) {
            tail = tail.substring(1);
        }
        return head + "/" + tail;
    }

    /**
     * Combine URL params to URI path.
     *
     * @param head the path head
     * @param params the URL params to append
     * @return the combined head and params
     */
    public static String combine(String head, Map<String, String> params) {
        String combined_params = "";
        if (params != null) {
           for (Entry<String, String> entry : params.entrySet()) {
                combined_params += String.format(PARAMETER_TEMPLATE, entry.getKey(), entry.getValue());
           }
        }
        return head + combined_params;
    }

    /**
     * A #Map sub-class which maps a model type (e.g. Tag.class) to a
     * set of suitable collection definitions.
     */
    private static class ModelToCollectionsMap extends HashMap<Class<? extends BaseResource>, ParentToCollectionMap> { }

    /**
     * A #Map sub-class which maps a parent model type to collection
     * definition.
     *
     * e.g. the map for Tag contains a collection definition for the
     * describing the VM, Host and User tags sub-collections. It also
     * contains a collection definition describing the top-level
     * tags collection which is keyed on the NO_PARENT key.
     */
    private static class ParentToCollectionMap extends LinkedHashMap<Class<? extends BaseResource>, Collection> {
        public ParentToCollectionMap(Class<?> resourceType,
                                     Class<?> collectionType,
                                     Class<? extends BaseResource> parentType) {
            super();
            add(resourceType, collectionType, parentType);
        }

        public ParentToCollectionMap(Class<?> resourceType,
                                     Class<?> collectionType) {
            this(resourceType, collectionType, NO_PARENT);
        }

        public void add(Class<?> resourceType,
                        Class<?> collectionType,
                        Class<? extends BaseResource> parentType) {
            put(parentType, new Collection(resourceType, collectionType, parentType));
        }
    }

    /**
     * A description of a collection type, its resource type and the parent
     * resource which contains it, if any.
     *
     * e.g. for the VM tags collection, resourceType is AssignedTagResource,
     * collectionType is AssignedTagsResource and parentType is VM
     */
    private static class Collection {
        private final Class<?> resourceType;
        private final Class<?> collectionType;
        private final Class<?> parentType;

        public Collection(Class<?> resourceType, Class<?> collectionType, Class<?> parentType) {
            this.resourceType = resourceType;
            this.collectionType = collectionType;
            this.parentType = parentType;
        }

        public Class<?> getResourceType()      { return resourceType; }
        public Class<?> getCollectionType()    { return collectionType; }
        public Class<?> getParentType()        { return parentType; }
    }

    /**
     * Used to specify link options
     */
    public enum LinkFlags { NONE, SEARCHABLE; }
}
