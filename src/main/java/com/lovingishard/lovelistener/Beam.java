package com.lovingishard.lovelistener;

import com.google.common.base.Objects;

/**
 * A beam of (spiritual) light. Example, a report of someone loving their neighbor.
 */
public interface Beam {
    double getLatitude();

    double getLongitude();

    String getDetail();

    long getTime();

    class Impl implements Beam {
        final double latitude;
        final double longitude;
        final long time;
        final String detail;

        public Impl(double latitude, double longitude, long time, String detail) {
            this.latitude = latitude;
            this.longitude = longitude;
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
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("time", time)
                    .add("latitude", latitude)
                    .add("longitude", longitude)
                    .add("detail", detail)
                    .toString();
        }
    }
}
