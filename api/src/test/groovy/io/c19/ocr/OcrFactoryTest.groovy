/*
 * Copyright (c) 2018 - 2019, C19, all rights reserved.
 *
 * This software is licensed under under GPL-3.0-only or GPL-3.0-or-later, https://opensource.org/licenses/GPL-3.0
 */

package io.c19.ocr

import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.Future

class OcrFactoryTest extends Specification
{
    @Shared OcrConfig config = new OcrConfig()
    @Shared OcrFactory instance = new OcrFactory( 4, config )

    def setupSpec()
    {
        instance.start()
    }

    def cleanupSpec()
    {
        instance.shutdown()
    }

    def "Process images"()
    {
        given:
        List<Future<String>> result = new ArrayList<>()
        long start = System.currentTimeMillis()

        when:
        for( int i=0;i<100;i++)
        {
            result.add( instance.processAsync( image ) )
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

    def "Process images from inputstream"()
    {
        given:
        int size = 100;
        List<Future<String>> result = new ArrayList<>()
        List<InputStream> images = new ArrayList<>()
        for( int i=0;i<size;i++)
        {
            images.add( new FileInputStream( image ) )
        }
        long start = System.currentTimeMillis()

        when:
        for( int i=0;i<size;i++)
        {
            result.add( instance.processAsync( images.get(i) ) )
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
