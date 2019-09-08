/*
 * Copyright (c) 2018 - 2019, C19, all rights reserved.
 *
 * This software is licensed under under GPL-3.0-only or GPL-3.0-or-later, https://opensource.org/licenses/GPL-3.0
 */

package io.c19.ocr.rest


import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

class OcrRestServiceDockerTest extends Specification
{

    @Shared OcrRestClient client = new OcrRestClient()

    def setupSpec()
    {
        client.setTarget( "http://35.228.160.238")
    }

    def cleanupSpec()
    {
    }

    @Unroll
    def "Post image as inputstream"()
    {
        given:
        InputStream input = new FileInputStream( image )

        when:
        String actual = client.ocr( input )

        then:
        actual.contains( expected )

        where:
        image || expected
        "src/test/resources/test00.png" || "Running Tesseract"
    }

    def "Process images from inputstream"()
    {
        given:
        int size = 100
        ExecutorService service = Executors.newFixedThreadPool(  16 )

        List<Future<String>> result = new ArrayList<>()

        long start = System.currentTimeMillis()

        when:
        for( int i=0;i<size;i++)
        {
            result.add( (Future<String>)service.submit{ ->
                    OcrRestClient client = new OcrRestClient()
                    client.setTarget( "http://35.228.160.238")
                    return client.ocr(new FileInputStream( image ) )
            } )
        }

        then:
        for( Future<String> future: result )
        {
            future.get() == expected
        }
        long finish = System.currentTimeMillis()
        System.out.println( "Duration: " + (finish - start) )

        where:
        image || expected
        "src/test/resources/test00.png" || "Running Tesseract\n"
    }
}
