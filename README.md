King-Me
=======

An Android application for exploring, training, and competing using GPS data for bicycling.

Competition
-----------

- Strava

  Strava is an excellent website for competition; this may even be a valid endpoint for the data we collect, at least preliminarily. However, their app leaves much to be desired - it does not allow you to navigate along a course, track your progress against a segment, or any such information. Instead, it tracks only speed, distance, and time.

- Other websites

  There are various other websites which are similar to strava, but as far as I can tell, none realy compare.

- Garmin

  Garmin covers the opposite end of the spectrum; where strava has a superb web interface for consolidating your data and tracking your progress, Garmin lacks.However, it is the only dedicated cycling gps device available, and it is leaps and bounds ahead of any similar concept. Unfortunately, they exploit their monopoly here, and charge exorbitant prices for their devices. By providing the same functionality integrated into a phone, we can reach a much greater audience. Additionally, their hardware is riddled with bugs, which greatly detract from the experience.

Critical Points
---------------

In order to develop a product which is capable of rivaling the power of the Garmin Edge, a few features are necessary.

- Courses

  A user needs to be able to download a course and follow it on their phone. Preliminarily, a simple line should suffice, but eventually, a few more features should be added:
    - Navigation along a course.
      This is actually one of the places that Garmin suffers. One well known bug is that their application will fail to navigate you back on to the course; instead, it will navigate you back to the beginning if you need to recalculate.
    - Course repositories
    - Course ratings
    - For circuits, allow the user to start anywhere along the course

- Segments:

  These are particular to Strava, and they basically provide competition points for users. They are similar to courses, but smaller in scale. I think the difference between these two things should mostly be in level of competition: lights and traffic conditions may cause unsafe riding when competition is introduced into full courses, but it should be more reasonable on segments. Additionally, segments are more of "hotspots", rather than full rides. Users should be encouraged to consume both types of data. Here are a few of the requirements for segments:
    - Virtual Partner-esque functionality
    - Course-builder: find popular segments around you and generate a course around them
    - large segment repositories
    - leaderboards
    - ability to update segment goals against other users' times

- Data:

  Garmin has a multitude of fields that can be observed while riding. Here is a partial list:
    - elevation / total ascent/descent
    - grade
    - speed / avg / max
    - distance
    - time
    - laps ( and data broken up by them )
    - course info (dist/time to dest/next and a whole bunch of others)
    - global info (sunrise/sunset/t.o.d/...)
    - calories
    - sensor based data:
        - heart rate ( and all sorts of averages, etc.)
        - power ( and all sorts of averages, etc.)
        - cadence ( and all sorts of averages, etc.)

- Sensors:

  This is low priority, since much information can be gathered directly from the device. However, if the application is reasonably fleshed out, adding support for heart rate monitors / power meters / speed / cadence sensors would be a welcome feature.