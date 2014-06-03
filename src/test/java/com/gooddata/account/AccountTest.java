package com.gooddata.account;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class AccountTest {

    @Test
    public void testDeserialize() throws Exception {
        final Account account = new ObjectMapper()
                .readValue(getClass().getResourceAsStream("/account/account.json"), Account.class);
        assertThat(account, is(notNullValue()));

        assertThat(account.getId(), is("ID"));
        assertThat(account.getSelfLink(), is("/gdc/account/profile/ID"));
        assertThat(account.getProjectsLink(), is("/gdc/account/profile/ID/projects"));
    }

}