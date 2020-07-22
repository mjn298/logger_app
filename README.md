#Logger App
as an aside - I regret how large the JAR file is. As I write this, I'm hoping it doesn't exceed the 
attachment limit.
## Usage:
This requires the current LTS JDK 11, it will certainly fail with JDK 8 (I tried) 
and will probably fail with anything in between.
From a shell, run `./run.sh`. It will prompt you to enter a "requests per second" threshold. As specified, the
default is 10. 

### Testing:
There are a couple of unit tests, for the Alerting logic and the data structure supporting the alerting logic.
These are run as part of the packaging step (`sbt assembly`). The easiest way to run them is with intellij's scala plugin,
and importing the SBT project. I test the four alert states (cartesian product of two booleans; isActive and isExceeded), 
as well as the proper updating of the TimestampMap. 

## Observations:
1. Due to the fact that alert state is updated per request (not per second of requests), the hit count when an alert becomes active
will always be threshold * 120. I couldn't think of another way to handle alerts coming in out of order, 
other than to bucket them by second. 
2. I had to come up with a hackish solution to handle requests' being out of order. I described this in a comment, but
I maintain buckets of both individual seconds and ten second increments. The start of the first ten second increment is the first log line's timestamp. I allow at most 2 ten second buckets. When a third one enters, ie
when bucket3's timestamp is >= bucket0's timestamp + 20, I pop bucket0 off the queue and mark it as ready to print.
This causes the final two buckets not to be printed. You'll also notice a consequence of this when 
an alert gets printed before a bucket which may have a timestamp before the alert. I could probably mitigate this by choosing a tolerance, and when 
a log line had a timestamp > bucket0.timestamp + 5, I'd pop bucket 0. This would limit the lost data, but not eliminate it.
Most directly, I need to figure out how to operate on the computed state when an FS2 stream finishes, I have not yet figured that out,
but I've already allowed myself an unreasonably long amount of time to indulge my curiosity and interest about 
these libraries.  
3. The shortcomings described above emerged from my desire to make this "real time" and decoupled from the actual data source.
It could be anything, and this will still work. The stream-building and deserialization plumbing would need to be adjusted (this is the benefit of something like Alpakka)
but once the welding happens (please excuse this extended metaphor) the data can flow. And, the shortcoming is due to my
unfamiliarity with the library - not the library itself. The data is consumed in one pass, and events are emitted as soon as application state
demands.  

## Philosophy:
There is absolutely no mutation in this application. Each log line results in a new state. That was done both as 
an exercise, and with an eye towards concurrency, if request info is being fielded from a number of different sources, there will be problems.
The LogGroupStats can be seen as a path towards CRDT with a "combine" operation. My preference in designing applications is to
define my domain model first, then implement the API, then implement IO. I did so here, hence the very explicit data types and their complete
isolation from IO. 

I first started writing this project using Akka and it quickly became unwieldy and an emblem of "overengineering", considering that
concurrency may be a future concern but was absolutely out of scope. I pivoted to Cats, Cats Effect, and FS2,
three closely related libraries I've been really interested in checking out. It took me a while to wrap my head
around them all, and I still haven't fully. It will take much more time, but I'm grateful to have been given
a project suiting them quite well.  

As per the specification, the file is streamed, and not read into working memory. 

## Improvements:
- This is not tested nearly enough. The alerting state logic is tested as requested. Additionally, I tested the TimestampMap, because its
correctness is essential to the Alerting's proper functioning, and its code is the most likely to face bugs. There's a lot more I'd like to cover
(anything involving arithmetic). Ease of testing is a major motivation to avoid mutation and restrict side-effectful
code to a small part of the app (in this case, the `Logger` object). Were this a production application, I'd test everything. 
- I should make the input path configurable so that you can try different files without recompiling... or worse, having to
install SBT and Scala if you don't have it. However, IntelliJ is impressive with self-containing Scala applications,
and you should be able to run it from IntelliJ directly after installing its Scala plugin.
- The greatest pain points in this application relate to maintaining the map structures. I cull the timestamp Map (used for alerting) based on currentTimestamp - 120 + a buffer of 10 seconds, meaning
everything from within, by default the past 130 seconds remains in the map. I do this to keep the in-memory storage to
a reasonable size. The tolerance exists to accommodate out of order requests, so I don't miss an alert. In a real application, with this service running concurrently in many different hosts, I'd probably use a Key Value store as a cache.
Otherwise, the application is pretty simple and most of the operations are just incrementing values by 1. 

## Total Time Spent:
I'd estimate I spent 4-6 hours actually coding this. I was fluent in several aspects of the application, and those were done quite quickly.
Determing proper (or just working) usage of some novel things made this take longer. 

I spent much more time working through reference materials, and experimenting. I would have done that eventually anyway,
this project just happened to be a good application for these techniques. 


