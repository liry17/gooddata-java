package com.gooddata.gdc;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class GdcTest {

    @Test
    public void testDeserialize() throws Exception {
        final Gdc gdc = new ObjectMapper().readValue(getClass().getResourceAsStream("/gdc/gdc.json"), Gdc.class);

        assertThat(gdc, is(notNullValue()));
        assertThat(gdc.getHomeLink(), is("/gdc/"));
        assertThat(gdc.getTokenLink(), is("/gdc/account/token"));
        assertThat(gdc.getLoginLink(), is("/gdc/account/login"));
        assertThat(gdc.getMetadataLink(), is("/gdc/md"));
        assertThat(gdc.getXTabLink(), is("/gdc/xtab2"));
        assertThat(gdc.getAvailableElementsLink(), is("/gdc/availableelements"));
        assertThat(gdc.getReportExporterLink(), is("/gdc/exporter"));
        assertThat(gdc.getAccountLink(), is("/gdc/account"));
        assertThat(gdc.getProjectsLink(), is("/gdc/projects"));
        assertThat(gdc.getToolLink(), is("/gdc/tool"));
        assertThat(gdc.getTemplatesLink(), is("/gdc/templates"));
        assertThat(gdc.getReleaseInfoLink(), is("/gdc/releaseInfo"));
        assertThat(gdc.getUserStagingLink(), is("https://STAGING"));
    }
}