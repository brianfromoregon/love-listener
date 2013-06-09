package com.lovingishard.lovelistener;

import com.google.common.base.Objects;

/**
 * A beam of (spiritual) light. Example, a report of someone loving their neighbor.
 */
public interface Beam {

    double getLatitude();

    double getLongitude();

    /**
     * Description of this beam, optional.
     */
    String getDetail();

    /**
     * When this beam was sent out, millis since epoch
     * @return
     */
    long getTime();

    /**
     * How was this beam generated
     *
     * 0 - fake
     * 1 - twitter
     * 2 - android app Beams
     */
    int getSource();

    class Impl implements Beam {
        final double latitude;
        final double longitude;
        final int source;
        final long time;
        final String detail;

        public Impl(double latitude, double longitude, int source, long time, String detail) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.source = source;
            this.time = time;
            this.detail = detail;
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

        public String getDetail() {
            return detail;
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
                    .add("detail", detail)
                    .toString();
        }
    }
}
