from django.db import models

# Create your models here.


class Ride(models.Model):
    ride_data = models.StringField
    ride_date = models.DateTimeField('date ridden')
