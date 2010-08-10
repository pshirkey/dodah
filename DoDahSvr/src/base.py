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
from django.utils import simplejson as json
from google.appengine.api import mail 
from django.core.validators import email_re
import lilcookies
import hashlib
import django.newforms as forms
import random
from googlemaps import GoogleMaps
import facebook

METERS_IN_MILE = 1609.344

class Base(webapp.RequestHandler):
    
    template_path = '/templates/'
    login_template_name = "login.html"

    def get(self):
        self.post()
    def post(self):
        if self.current_user:
            self.do_post()
        else:
            self.generate( Base.login_template_name, None )
            
    def do_post(self):
        pass    
    
    def param(self, name):
        return self.request.get(name)
    
    def write(self, mes):
        self.response.out.write(mes)
        
    def _create_item_code(self):
        alphabet = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'
        return "".join(random.sample(alphabet,8))
    
    def generate (self, fileIn, values):
        if values is None:
            values = {}
        
        if 'current_user' not in values.keys():
            values['current_user'] = self.current_user
        if 'site_name' not in values.keys():
            values['site_name'] = self.enviroment.site_name
        if 'site_url' not in values.keys():
            values['site_url'] = self.enviroment.external_url
        if 'fb_api_key' not in values.keys():
            values['fb_api_key'] = self.enviroment.facebook_api_key
        
        
        ROOT_PATH = os.path.dirname(__file__)
        path = os.path.join(ROOT_PATH + self.template_path, str(fileIn))
        self.response.out.write(template.render(path, values))
        
    @property
    def enviroment(self):
        if not hasattr(self, "_enviroment"):
            self._enviroment = None
            self._enviroment = models.Environment.get_for_current_environment()
        return self._enviroment;  
    
    @property
    def current_user(self):
        if not hasattr(self, "_current_user"):
            self._current_user = None
            #host = os.environ.get("SERVER_NAME")
            #if host == "localhost":
            #    self._current_user = models.User.get_test_user()
            #    return self._current_user      
            
            cookieutil = lilcookies.LilCookies(self, self.enviroment.cookie_secret )
            uid = cookieutil.get_secure_cookie(name='uid')
            if uid:
                self._current_user = db.get(uid)
        return self._current_user
    
    def update_current_user_facebook(self):
        cookie = facebook.get_user_from_cookie(
                        self.request.cookies, self.enviroment.facebook_api_key, self.enviroment.facebook_secret_key)
        if cookie and self.current_user:
            graph = facebook.GraphAPI(cookie["access_token"])
            profile = graph.get_object("me")
            self.current_user.fb_uid = profile["id"]
            self.current_user.fb_profile_url = profile["link"]
            self.current_user.fb_access_token = cookie["access_token"]
            self.current_user.save() 