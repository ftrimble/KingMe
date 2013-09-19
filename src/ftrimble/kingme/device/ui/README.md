# The King Me UI

The first strong point of the King Me UI is the ability to swap out the
data being viewed very fluidly. This will require some nonstandard development
work. First, consider the format of a data view:
----------------
|  Description |
----------------
| Data | Units |
----------------

This requires some non-trivial typing to type over and over again in the layout
files, so subclassing the linearlayout class to encapsulate all of this
information should significantly improve the overhead here.

Additionally, by subclassing this field, we can give information as to what sort
of data is contained in the field; in this manner, we can loop through the
visible views and update their data according to what they are showing, rather
than finding all views and updating them.

The next problem is that the number of actual fields is variant. We might have
12 fields on a page or we might have 3. It seems like the logical next step is
to additionally subclass the layout that contains all of the data views and
call methods on the class to ensure that it is handled according to the
settings.