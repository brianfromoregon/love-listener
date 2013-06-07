package com.lovingishard.lovelistener;

import lang.Loggers;
import org.slf4j.Logger;


public class Main {
    static final Logger log = Loggers.contextLogger();

    public static void main(String[] args) {

        TwitterStreamer streamer = new TwitterStreamer();
        PoorManPubsub.CanBeUpdated<Beam> beams = streamer.getBeams().mergeWith(sampleBeams());

        beams.map(String::valueOf).forEach(log::info);

        new PoorManPubsub.Node<Beam>(beams) {
            @Override
            protected void react(Beam beam) {
                log.info("I am writing this value to a db: " + beam);
            }
        };

        streamer.start();
    }

    private static PoorManPubsub.CanBeUpdated<Beam> sampleBeams() {
        final PoorManPubsub.CanBeUpdated<Beam> beams = new PoorManPubsub.CanBeUpdated<>();

        new Thread(new Runnable() {
            int x = 0;

            @Override
            public void run() {
                while (true) {
                    beams.update(new Beam.Impl(x++, x++, x++, String.valueOf(x++)));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new AssertionError();
                    }
                }
            }
        }).start();

        return beams;
    }
}
