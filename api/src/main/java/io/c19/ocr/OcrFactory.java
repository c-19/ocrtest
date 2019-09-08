/*
 * Copyright (c) 2018 - 2019, C19, all rights reserved.
 *
 * This software is licensed under under GPL-3.0-only or GPL-3.0-or-later, https://opensource.org/licenses/GPL-3.0
 */

package io.c19.ocr;

import java.util.UUID;
import java.util.concurrent.*;

public class OcrFactory
{

    private ExecutorService executor;
    private ExecutorService resultRetriever;
    private OcrConfig config;
    private int threads;

    private ConcurrentLinkedQueue<OcrParams> images = new ConcurrentLinkedQueue<>();
    private ConcurrentHashMap<String,String> results = new ConcurrentHashMap<>();

    public OcrFactory( int threads, OcrConfig config )
    {
        this.threads = threads;
        this.config = config;
    }

    public void start()
    {
        executor = Executors.newFixedThreadPool( threads );
        resultRetriever =  Executors.newFixedThreadPool( threads );
        for( int i=0; i< threads; i++ )
        {
            executor.execute(new OcrProcessorTask(this.config));
        }
    }

    public void shutdown()
    {
        executor.shutdownNow();
        resultRetriever.shutdownNow();
    }

    public Future<String> processAsync( String image )
    {
        OcrParams params = new OcrParams( image );

        images.add( params );

        return this.resultRetriever.submit( new OcrResult( params ) );
    }

    public String process( String image )
    {
        try
        {
            return processAsync(image).get();
        }
        catch( InterruptedException | ExecutionException e )
        {
            throw new RuntimeException( "Error processing", e );
        }
    }

    private class OcrProcessorTask implements Runnable
    {
        private Ocr processor;

        private OcrProcessorTask( OcrConfig config )
        {
            processor = new Ocr( config );
        }

        @Override
        public void run()
        {
            try
            {
                processor.init();
                while (true) {
                    try {
                        OcrParams params = images.poll();
                        if (params != null) {
                            String result = processor.process(params.image);
                            results.put(params.corrId, result);
                        }
                        Thread.sleep(1);
                    }
                    catch (InterruptedException e) {
                        break;
                    }
                }
            }
            finally
            {
                processor.stop();
            }


        }
    }

    private class OcrResult implements Callable<String>
    {

        private String corrId;

        private OcrResult( OcrParams params )
        {
            corrId = params.corrId;
        }

        @Override
        public String call()
        {
            while( true )
            {
                try
                {
                    if( results.containsKey( corrId ) )
                    {
                        return results.remove( corrId );
                    }
                    Thread.sleep( 1 );
                }
                catch (InterruptedException e)
                {
                    break;
                }
            }
            throw new RuntimeException( "Interrupted before result for corrid " + corrId + "could be retrieved." );
        }
    }

    private class OcrParams
    {
        private String corrId;
        private String image;

        private OcrParams( String image )
        {
            this.image = image;
            this.corrId = UUID.randomUUID().toString();
        }
    }

}
