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
    
    def post(self):
        access_token = self.param('access_token')
        service_client = self.param('service_client')
        if access_token and service_client:
            token = models.AccessToken.valid(access_token)
            client = models.ServiceClient.valid(service_client)
            if token and client:
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
        diffs = {}
        models.Difficulty.load()
        for d in models.Difficulty.all():             
            diffs[d.name] = d.time_to_find_seconds
        json.append(diffs)
        self.write_json(json)
                
     
application = webapp.WSGIApplication([
                                      (Authenticate.url, Authenticate),
                                      (CreateAccount.url, CreateAccount),
                                      (GetDifficulties.url, GetDifficulties)                                                          
                                     ], debug=True)



def main():
    run_wsgi_app(application)

if __name__ == "__main__":
    main()