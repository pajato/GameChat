# Use of Firebase Cloud Functions

Pricing for cloud functions is (monthly): free for the first 2 million function invocations, $0.40/million thereafter.

How many messages and game event moves is that?

Let's assume 1 million Users, each generating 100 messages and game moves per day. That is 100 million functions per day, or 3 billion functions per month.

=> 3000 Million functions per month (ignoring the 2 million free ones) => 3000 * .40 == $1200/month to handle notifications.

For 1 million Users at a 60% subscription rate  ==> 600K * $5 / 12 == $250K/month.

Put another way, at an average of 3K functions per month per User, 3M functions per month (essentially free) is 3M/3K => the first 1K Users are essentially free.

Thereafter, 1M functions/month == $0.40, 1M / 3K fpm => $0.40 per 333 Users after the first 1K Users, or $1.20 per thousand Users.
