# King Me


_An Android application for exploring, training, and competing using GPS data
    for bicycling._

## Critical Features

In order to develop a product which is capable of rivaling the power of the
Garmin Edge, a few features are necessary.

- Courses

  A user needs to be able to download a course and follow it on their phone.
  Preliminarily, a simple line should suffice, but eventually, a few more
  features should be added:
    - Navigation along a course.
      This is actually one of the places that Garmin suffers. One well known
      bug is that their application will fail to navigate you back on to the
      course; instead, it will navigate you back to the beginning if you need
      to recalculate.
    - Course repositories
    - Course ratings
    - For circuits, allow the user to start anywhere along the course
    - when following a course, it should find all the segments that the course
      goes through, and give you warning 30 seconds before you get to the
      segment. Once you hit the segment, it should give you real time updates
      in a virtual partner format against the segment leader.

- Segments:

  These are particular to Strava, and they basically provide competition points
  for users. They are similar to courses, but smaller in scale. I think the
  difference between these two things should mostly be in level of competition:
  lights and traffic conditions may cause unsafe riding when competition is
  introduced into full courses, but it should be more reasonable on segments.
  Additionally, segments are more of "hotspots", rather than full rides. Users
  should be encouraged to consume both types of data. Here are a few of the
  requirements for segments:
    - Virtual Partner-esque functionality
    - Course-builder: find popular segments around you and generate a course
      around them
    - large segment repositories
    - leaderboards
    - ability to update segment goals against other users' times

- Data:

  Garmin has a multitude of fields that can be observed while riding. Here is
  a partial list:
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

  This is low priority, since much information can be gathered directly from
  the device. However, if the application is reasonably fleshed out, adding
  support for heart rate monitors / power meters / speed / cadence sensors
  would be a welcome feature.