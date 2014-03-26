from django.shortcuts import render
from django.http import HttpResponse
from django.template import RequestContext, loader

from rideupload.models import Ride

def feed(request):
    latest_ride_list = Ride.objects.order_by('-ride_date')[:5]
    template = loader.get_template('rideupload/feed.html')
    context = RequestContext(request, {
        'latest_ride_list': latest_ride_list,
    })
    return HttpResponse(template.render(context))
