package com.datn.backend.dto.request;

    public class HuyHoaDonRequest {
        private String lyDo;

        // Constructors
        public HuyHoaDonRequest() {}

        public HuyHoaDonRequest(String lyDo) {
            this.lyDo = lyDo;
        }

        // Getters and Setters
        public String getLyDo() {
            return lyDo;
        }

        public void setLyDo(String lyDo) {
            this.lyDo = lyDo;
        }
    }

