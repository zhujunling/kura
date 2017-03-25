/*******************************************************************************
 * Copyright (c) 2011, 2017 Eurotech and/or its affiliates
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech
 *******************************************************************************/

package org.eclipse.kura.demo.bnd.heater;

import org.osgi.service.metatype.annotations.*;

/**
 * Meta type information for {@link Heater}
 * <p>
 * <strong>Note: </strong> The id must be the full qualified name of the assigned component.
 * </p>
 */
@ObjectClassDefinition(
        id = "org.eclipse.kura.demo.bnd.heater.Heater",
        name = "Heater Config",
        icon = @Icon(resource = "heater.png", size = 32),
        description = "This is the configuration of the Heater demo."
)
@interface HeaterConfig {

    @AttributeDefinition(
            name = "mode",
            type = AttributeType.STRING,
            defaultValue = "Program",
            description = "Operating mode for the heater. If operatng mode is Vacation, set point is automatiaclly set to 6.0C.",
            options = {
                    @Option(label = "Program", value = "Program"),
                    @Option(label = "Manual", value = "Manual"),
                    @Option(label = "Vacation", value = "Vacation"),
            }
    )
    String mode();

    @AttributeDefinition(
            name = "program.startTime",
            type = AttributeType.STRING,
            required = false,
            defaultValue = "06:00",
            description = "Start time for the heating cycle with the operating mode is Program."
    )
    String program_startTime();

    @AttributeDefinition(
            name = "program.stopTime",
            type = AttributeType.STRING,
            required = false,
            defaultValue = "22:00",
            description = "Stop time for the heating cycle with the operating mode is Program."
    )
    String program_stopTime();

    @AttributeDefinition(
            name = "program.setPoint",
            type = AttributeType.FLOAT,
            required = false,
            defaultValue = "20.5",
            min = "5.0",
            max = "40.0",
            description = "Temperature Set Point in Celsius for the heating cycle with the operating mode is Program."
    )
    String program_setPoint();

    @AttributeDefinition(
            name = "manual.setPoint",
            type = AttributeType.FLOAT,
            required = false,
            defaultValue = "15.0",
            min = "5.0",
            max = "40.0",
            description = "Temperature Set Point in Celsius for the heating cycle with the operating mode is Manual."
    )
    String manual_setPoint();

    @AttributeDefinition(
            name = "temperature.initial",
            type = AttributeType.FLOAT,
            required = false,
            defaultValue = "10",
            description = "Initial value for the temperature metric."
    )
    String temperature_initial();

    @AttributeDefinition(
            name = "temperature.increment",
            type = AttributeType.FLOAT,
            required = false,
            defaultValue = "0.25",
            description = "Increment value for the temperature metric."
    )
    String temperature_increment();

    @AttributeDefinition(
            name = "publish.rate",
            type = AttributeType.INTEGER,
            defaultValue = "2",
            min = "1",
            description = "Default message publishing rate in seconds (min 1)."
    )
    String publish_rate();

    @AttributeDefinition(
            name = "publish.semanticTopic",
            type = AttributeType.STRING,
            defaultValue = "data",
            description = "Default semantic topic to publish the message to."
    )
    String publish_semanticTopic();

    @AttributeDefinition(
            name = "publish.qos",
            type = AttributeType.INTEGER,
            defaultValue = "0",
            description = "Default QoS to publish the message with.",
            options = {
                    @Option(label = "Fire and forget", value = "0"),
                    @Option(label = "At least once", value = "1"),
                    @Option(label = "At most once", value = "2")
            }
    )
    String publish_qos();

    @AttributeDefinition(
            name = "publish.retain",
            type = AttributeType.BOOLEAN,
            defaultValue = "false",
            description = "Default retaining flag for the published message."
    )
    String publish_retain();
}
