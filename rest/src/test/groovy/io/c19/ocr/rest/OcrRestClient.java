/*
 * Copyright (c) 2018 - 2019, C19, all rights reserved.
 *
 * This software is licensed under under GPL-3.0-only or GPL-3.0-or-later, https://opensource.org/licenses/GPL-3.0
 */

package io.c19.ocr.rest;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.concurrent.Future;

public class OcrRestClient
{

    private String mTarget = "http://workflow-fkskosa.engine-dev1.sfa.se";

    private String userId;

    private final Client mClient;

    public OcrRestClient()
    {
        mClient = ClientBuilder.newClient();
    }

    public String getTarget()
    {
        return mTarget;
    }

    public void setTarget(String target)
    {
        mTarget = target;
    }

    public String ocr( InputStream stream )
    {
        WebTarget target = mClient.target(getTarget()).path( "/ocr" );

        Response response = target.request().post( Entity.entity( stream, MediaType.APPLICATION_OCTET_STREAM ) );
        handleFailures( response );

        return response.readEntity( String.class );
    }

    private void handleFailures(Response response)
    {
        Response.StatusType status = response.getStatusInfo();
        if (status.getFamily() != Response.Status.Family.SUCCESSFUL)
        {
            String message = response.toString();
            message += response.readEntity(String.class);


            throw new RuntimeException( message );
        }
    }
}
