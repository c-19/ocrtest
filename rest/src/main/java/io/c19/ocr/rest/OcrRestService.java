/*
 * Copyright (c) 2018 - 2019, C19, all rights reserved.
 *
 * This software is licensed under under GPL-3.0-only or GPL-3.0-or-later, https://opensource.org/licenses/GPL-3.0
 */

package io.c19.ocr.rest;

import io.c19.ocr.OcrConfig;
import io.c19.ocr.OcrFactoryV2;

import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.InputStream;

@Stateless
@Path("/")
public class OcrRestService
{
    private static OcrFactoryV2 factory;

    static
    {
        factory = new OcrFactoryV2( 4, new OcrConfig() );
        factory.start();
    }

    public OcrRestService()
    {
        //CDI no-args
    }

    @GET
    @Path("")
    public Response get()
    {
        return Response.ok("UP").build();
    }


    @POST
    @Path( "ocr" )
    public Response ocr( InputStream data )
    {
        try
        {
            String result = factory.process( data );
            return Response.ok( result ).build();
        }
        catch( RuntimeException e )
        {
            return Response.serverError( ).entity( e.getLocalizedMessage() ).build();
        }
    }
}
