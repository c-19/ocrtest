/*
 * Copyright (c) 2018 - 2019, C19, all rights reserved.
 *
 * This software is licensed under under GPL-3.0-only or GPL-3.0-or-later, https://opensource.org/licenses/GPL-3.0
 */

package io.c19.ocr;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.*;

public class OcrFactoryV1_1
{

    private ExecutorService executor;
    private ExecutorService resultRetriever;
    private OcrConfig config;
    private int threads;

    private BlockingQueue<OcrParams> images = new LinkedBlockingQueue<>();
    private ConcurrentHashMap<String,String> results = new ConcurrentHashMap<>();

    public OcrFactoryV1_1(int threads, OcrConfig config )
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

        return processAsync( params );
    }

    public Future<String> processAsync( InputStream stream )
    {
        OcrParams params = new OcrParams( stream );

        return processAsync( params );
    }

    private Future<String> processAsync( OcrParams params )
    {
        images.add( params );

        CompletableFuture t = new CompletableFuture<>();


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

    public String process( InputStream image )
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
                while (true)
                {
                    try
                    {
                        OcrParams params = images.take();

                        String result = params.isFile() ? processor.process(  params.file) : processor.process( params.image );
                        results.put(params.corrId, result);
                    }
                    catch (InterruptedException e)
                    {
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
        private InputStream image;
        private String file;

        private OcrParams()
        {
            this.corrId = UUID.randomUUID().toString();
        }

        private OcrParams( String file )
        {
            this();
            this.file = file;
        }

        private OcrParams( InputStream image )
        {
            this();
            this.image = image;
        }

        private boolean isFile()
        {
            return this.file != null;
        }
    }

}
