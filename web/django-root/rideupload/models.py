from django.db import models

# Create your models here.

class User(models.Model):
    username = models.CharField(max_length=15)

class Ride(models.Model):
    ride_data = models.CharField(max_length=200)
    ride_date = models.DateTimeField('date ridden')
    rider = models.ForeignKey('User')

class Segment(models.Model):
    gpx_coords = models.CharField(max_length=200)

class Goal(models.Model):
    segment = models.ForeignKey(Segment)
    user = models.ForeignKey(User)
    timeGoal = models.IntegerField()

