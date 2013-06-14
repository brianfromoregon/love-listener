package com.lovingishard.lovelistener;

import com.google.common.base.Objects;

/**
 * A beam of (spiritual) light. Example, a report of someone loving their neighbor.
 */
public interface Beam {

    double getLatitude();

    double getLongitude();

    /**
     * Message from the user, optional.
     */
    String getMessage();

    /**
     * When this beam was reported, millis since epoch
     */
    long getTime();

    /**
     * How was this beam generated
     * <p/>
     * 0 - fake
     * 1 - twitter #lovebeam
     * 2 - android app Beams
     */
    int getSource();

    class Impl implements Beam {
        final double latitude;
        final double longitude;
        final int source;
        final long time;
        final String message;

        public Impl(double latitude, double longitude, int source, long time, String message) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.source = source;
            this.time = time;
            this.message = message;
        }

        public long getTime() {
            return time;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public int getSource() {
            return source;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("latitude", latitude)
                    .add("longitude", longitude)
                    .add("source", source)
                    .add("time", time)
                    .add("message", message)
                    .toString();
        }
    }

    class Random extends Impl {
        public Random() {
            super(Math.random() * -180.0 + 90.0, Math.random() * -360.0 + 180.0, 0, System.currentTimeMillis(), null);
        }
    }
}
