from django.conf.urls import patterns, url

from rideupload import views

urlpatterns = patterns('', url(r'^$', views.index, name='index'))
