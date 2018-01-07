/*******************************************************************************
 * Copyright (c) 2011, 2016 Eurotech and/or its affiliates
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech
 *******************************************************************************/
package org.eclipse.kura.cloud;

import org.osgi.annotation.versioning.ProviderType;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
@ProviderType
public class CloudletTopic {

    public enum Method {
        GET, PUT, POST, DEL, EXEC;
    }

    private Method m_method;
    private String[] m_resources;

    public static CloudletTopic parseAppTopic(String appTopic) {
        CloudletTopic edcApplicationTopic = new CloudletTopic();

        String[] parts = appTopic.split("/");
        edcApplicationTopic.m_method = Method.valueOf(parts[0]);
        if (parts.length > 1) {

            edcApplicationTopic.m_resources = new String[parts.length - 1];
            for (int i = 0; i < edcApplicationTopic.m_resources.length; i++) {
                edcApplicationTopic.m_resources[i] = parts[i + 1];
            }
        }
        return edcApplicationTopic;
    }

    private CloudletTopic() {
        super();
    }

    public Method getMethod() {
        return this.m_method;
    }

    public String[] getResources() {
        return this.m_resources;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.m_method.name());
        if (this.m_resources != null) {
            for (String m_resource : this.m_resources) {
                sb.append("/");
                sb.append(m_resource);
            }
        }
        return sb.toString();
    }
}
