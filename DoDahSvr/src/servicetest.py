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

class TestBase(webapp.RequestHandler):
    template_path = '/templates/service_test/'

    def get(self):
        self.post()
    def post(self):
        pass
    
    def param(self, name):
        return self.request.get(name)
    
    def write(self, mes):
        self.response.out.write(mes)
        
    def generate (self, fileIn, values):
        ROOT_PATH = os.path.dirname(__file__)
        path = os.path.join(ROOT_PATH + self.template_path, str(fileIn))
        self.response.out.write(template.render(path, values))
        
    @property
    def enviroment(self):
        if not hasattr(self, "_enviroment"):
            self._enviroment = None
            self._enviroment = models.Environment.get_for_current_environment()
        return self._enviroment;  

class Index(TestBase):
    
    url = "/test"
    url2 = "/test/"
    template = "index.html"
    
    def post(self):
        cleared_message = self.request.get('cleared_message')
        u = models.User.get_test_user()
        if u:
            at = models.AccessToken.create_for_user(u)
        self.generate(Index.template, {'cleared_message':cleared_message, 'test_user_access_token':at.token})
        
    
class Clear(TestBase):
    
    url = "/test/clear"
    
    def post(self):
        
        client = models.ServiceClient.valid("882AF324A4DC11DFB3BABF3DE0D72085")
        if client:
            try:
                for log in client.log:
                    if log.access_token:
                        if log.access_token.user:
                            try:
                                log.access_token.user.delete();
                            except:
                                pass    
                        try:                        
                            log.access_token.delete()
                        except:
                            pass
                    log.delete()
            except:
                pass                        
        self.redirect('test?cleared_message=data cleared %s' % datetime.datetime.now().strftime("%m/%d/%y %I:%M:%S"))
        

application = webapp.WSGIApplication([
                                      (Index.url, Index),
                                       (Index.url2, Index),
                                       (Clear.url, Clear)                                                        
                                     ], debug=True)



def main():
    run_wsgi_app(application)

if __name__ == "__main__":
    main()