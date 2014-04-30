/*
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.connectors;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 */
public interface Connector {
    @JsonIgnore
    String getConnectorName();
}
