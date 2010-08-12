import cgi
import logging
import os.path
import time
import urllib
import datetime
from data import models
from google.appengine.ext import db
from google.appengine.ext import webapp
from google.appengine.ext.webapp import template
from google.appengine.ext.webapp.util import run_wsgi_app
from google.appengine.ext.db import djangoforms
from django.utils import simplejson
from google.appengine.api import mail 
from django.core.validators import email_re
import lilcookies
import hashlib
import django.newforms as forms
import random
from googlemaps import GoogleMaps
import facebook
import base

class ServiceBase(base.Base):
    
    def __init__(self):
        self.user = None
        
    def post(self):
        access_token = self.param('access_token')
        service_client = self.param('service_client')
        if access_token and service_client:
            token = models.AccessToken.valid(access_token)
            client = models.ServiceClient.valid(service_client)
            if token and client:
                self.user = token.user
                models.ServiceLog.create(self.__class__.__name__, client, token)
                self.do_post()
            else:
                self.write_error('invalid access token or service client')  
        else:
            self.write_error('missing access token or service client')
        
    def do_post(self):
        pass
    
    def write_error(self, message ):
        self.response.out.write(simplejson.dumps([{ 'status':'error','error_message':message }]))
    
    def write_json(self, json_object_array): 
        self.response.out.write(simplejson.dumps(json_object_array))
    
    
class Authenticate(ServiceBase):
    
    url = "/service/authenticate"
    
    def post(self):        
        email = self.param('email')
        password = self.param('password')    
        service_client = self.param('service_client')
        if service_client:
            client = models.ServiceClient.valid(service_client)
            if client:
                log = models.ServiceLog.create(self.__class__.__name__, client, None)
                if email and password:    
                    user = models.User.get_by_key_name(email)
                    if user and user.password == password:
                        access_token = models.AccessToken.create_for_user(user)
                        log.access_token = access_token
                        log.save()
                        self.write_json([{ 'status':'success'},{'access_token':access_token.token }])
                    else:
                        self.write_error('invalid email or password')
                else:
                    self.write_error('missing email or password')
            else:
                self.write_error('invalid client')
                
class CreateAccount(ServiceBase):
    
    url = "/service/createaccount"
    
    def post(self):
        first_name = self.param('first_name')
        last_name = self.param('last_name')
        emailAddress = self.param('email')
        password = self.param('password')
        service_client = self.param('service_client')
        if service_client:
            client = models.ServiceClient.valid(service_client)
            if client:
                log = models.ServiceLog.create(self.__class__.__name__, client, None)
                if emailAddress and password:
                    if models.User.email_exists(emailAddress):
                        self.write_error("Email Address is already in use")
                        return
                    else:
                        user = models.User.create(first_name, last_name, emailAddress, password)
                        if user:
                            access_token = models.AccessToken.create_for_user(user)
                            log.access_token = access_token
                            log.save()
                            self.write_json([{ 'status':'success'},{'access_token':access_token.token }])
                        else:
                            self.write_error("Unable to create account")
            else:
                self.write_error('invalid client')
        else:
            self.write_error('invalid client')
            
class GetDifficulties(ServiceBase):
    
    url = "/service/getdifficulties"
    
    def do_post(self):
        json = [{ 'status':'success', }]
        models.Difficulty.load()
        for d in models.Difficulty.all():             
            json.append(d.to_json_object())
        self.write_json(json)
                
class StartSearch(ServiceBase):
    
    url = "/service/startsearch"
    
    def do_post(self):
        location_key = self.param('location_key')
        if location_key:
            try:
                location = db.get(location_key)
            except:
                self.write_error("Invalid Location Key")
                return
            
            if location:
                models.UserLog.create(self.user, 'Started Searching for DoDads at %s' % location.name, location=location)
                item = location.get_available_item()
                if item:
                    self.write_json([{ 'status':'success'},item.to_json_object()])
                else:
                    self.write_error('No Items available')
            else:
                self.write_error("Not a supported location")
            
        else:
            self.write_error("Missing Location Key")
            
class Found(ServiceBase):
    
    url = "/service/found"
    
    def do_post(self):
        item_key = self.param('item_key')
        if item_key:
            item = db.get(item_key)
            if item:
                item.found_user = self.user
                item.found_date_time = datetime.datetime.now()
                item.save()
                models.UserLog.create(self.user, "Found '%s' for DoDads at %s" % ( item.name, item.location.name ), location=item.location)
                self.write_json([{ 'status':'success'}])
            else:
                self.write_error("Invalid Item Key")
        else:
            self.write_error("Missing Item Key")
            
            
class Redeem(ServiceBase):
    
    url = "/service/redeem"
    
    def do_post(self):
        item_key = self.param('item_key')
        if item_key:
            item = db.get(item_key)
            if item:
                item.redeemed = True
                item.save()
                self.write_json([{ 'status':'success'}])
            else:
                self.write_error("Invalid Item Key")
        else:
            self.write_error("Missing Item Key")          
                
class WhatsAroundMe(ServiceBase):
    
    url = "/service/whatsaroundme"
    
    def do_post(self):
        address = self.param('address')
        lat = self.param('lat')
        lon = self.param('lon')
        miles = self.request.get('miles', default_value=10)
        max_results = self.request.get('max_results', default_value=10)
        geoPoint = None
        try:
            if address:
                try:
                    gmaps = GoogleMaps(self.enviroment.google_maps_key)   
                    lat, lon = gmaps.address_to_latlng(address)
                except:
                    self.write_error('Cannot get position from address')
                    return
            geoPoint = db.GeoPt(lat, lon)
        except:
            self.write_error('Error validating position')
            return
        
        if geoPoint:
            meters = float(miles) * base.METERS_IN_MILE        
            locations = models.Location.proximity_fetch( models.Location.all(), geoPoint, int(max_results), meters )
            if locations and len(locations) > 0:
                json = [{ 'status':'success', }]
                for loc in locations:             
                    json.append(loc.to_json_object())
                self.write_json(json)
            else:
                self.write_error("No Locations within %s miles" % miles)
            
     
application = webapp.WSGIApplication([
                                      (Authenticate.url, Authenticate),
                                      (CreateAccount.url, CreateAccount),
                                      (GetDifficulties.url, GetDifficulties),
                                      (StartSearch.url, StartSearch),
                                      (Found.url, Found),
                                      (Redeem.url, Redeem),
                                      (WhatsAroundMe.url, WhatsAroundMe)                                                       
                                     ], debug=True)



def main():
    run_wsgi_app(application)

if __name__ == "__main__":
    main()