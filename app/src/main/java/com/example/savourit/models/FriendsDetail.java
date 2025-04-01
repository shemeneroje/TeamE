package com.example.savourit.models;

public class FriendsDetail {
        private String userId;
        private String username;
        private String accountType;
        private String email;

        // **Empty Constructor (Required for Firebase)**
        public FriendsDetail() {
        }

        // **Parameterized Constructor**
        public FriendsDetail(String userId, String username, String accountType, String email) {
            this.userId = userId;
            this.username = username;
            this.accountType = accountType;
            this.email = email;
        }

        // **Getter and Setter Methods**
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getAccountType() {
            return accountType;
        }

        public void setAccountType(String accountType) {
            this.accountType = accountType;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

}
