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

package org.ovirt.engine.api.model;

public enum VmStatus {

    UNASSIGNED,
    DOWN,
    UP,
    POWERING_UP,
    POWERED_DOWN,
    PAUSED,
    MIGRATING,
    UNKNOWN,
    NOT_RESPONDING,
    WAIT_FOR_LAUNCH,
    REBOOT_IN_PROGRESS,
    SAVING_STATE,
    RESTORING_STATE,
    SUSPENDED,
    IMAGE_LOCKED,
    POWERING_DOWN;

    public String value() {
        return name().toLowerCase();
    }

    public static VmStatus fromValue(String v) {
        try {
            return valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
