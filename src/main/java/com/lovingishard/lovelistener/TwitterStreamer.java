package com.lovingishard.lovelistener;

import com.typesafe.config.Config;
import lang.Loggers;
import org.slf4j.Logger;
import twitter4j.*;
import twitter4j.conf.Configuration;
import twitter4j.conf.PropertyConfiguration;

import java.util.Properties;

/**
 *
 */
public class TwitterStreamer {
    static final Logger log = Loggers.contextLogger();

    private final PoorManPubsub.CanBeUpdated<Beam> beams = new PoorManPubsub.CanBeUpdated<>();

    private final Configuration config;

    public TwitterStreamer(Config topConf) {
        Config conf = topConf.getConfig("love-listener.twitter");
        Properties props = new Properties();
        props.setProperty("debug", conf.getString("debug"));
        props.setProperty("oauth.consumerKey", conf.getString("oauth.consumerKey"));
        props.setProperty("oauth.consumerSecret", conf.getString("oauth.consumerSecret"));
        props.setProperty("oauth.accessToken", conf.getString("oauth.accessToken"));
        props.setProperty("oauth.accessTokenSecret", conf.getString("oauth.accessTokenSecret"));
        config = new PropertyConfiguration(props);
    }

    public void start() {
        TwitterStream twitterStream = new TwitterStreamFactory(config).getInstance();
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

                beams.update(new Beam.Impl(loc.getLatitude(), loc.getLongitude(), 1, System.currentTimeMillis(), status.getText()));
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

    public PoorManPubsub.Stream<Beam> getBeams() {
        return beams;
    }
}
