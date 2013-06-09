package com.lovingishard.lovelistener;

import lang.Loggers;
import org.slf4j.Logger;
import twitter4j.*;

/**
 *
 */
public class TwitterStreamer {
    static final Logger log = Loggers.contextLogger();

    private final PoorManPubsub.CanBeUpdated<Beam> beams = new PoorManPubsub.CanBeUpdated<>();

    public void start() {
        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        StatusListener listener = new StatusListener() {
            @Override
            public void onStatus(Status status) {
                GeoLocation loc = status.getGeoLocation();
                if (loc == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Ignoring update without GeoLocation: " + status);
                    }
                    return;
                }

                beams.update(new Beam.Impl(loc.getLatitude(), loc.getLongitude(), System.currentTimeMillis(), status.getText()));
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                if (log.isDebugEnabled()) {
                    log.debug(statusDeletionNotice.toString());
                }
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                log.warn("onTrackLimitationNotice(%d) called", numberOfLimitedStatuses);
            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
                // Unsupported
            }

            @Override
            public void onStallWarning(StallWarning warning) {
                log.warn(warning.toString());
            }

            @Override
            public void onException(Exception ex) {
                log.warn("Exception received by Twitter StatusListener", ex);
            }
        };
        twitterStream.addListener(listener);
        twitterStream.filter(new FilterQuery(0, null, new String[] {Const.twitterHashTag}));
    }

    public PoorManPubsub.CanBeUpdated<Beam> getBeams() {
        return beams;
    }
}
