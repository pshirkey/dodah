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
        
    def _create_item_code(self):
        alphabet = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'
        return "".join(random.sample(alphabet,8))
        
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
    
    @property 
    def test_location(self):
        if not hasattr(self, "_test_location"):
            self._test_location = None
            if models.Category.all().count() <= 0:
                cat = models.Category(name="Food/Beverage", description="")
                cat.put()
                
                subcat = models.Category(name="Resturant", description="", parent_category=cat)
                subcat.put()
                
                subcat = models.Category(name="Coffee House", description="", parent_category=cat)
                subcat.put()
            
            self._test_location = models.Location.gql("WHERE name='Some Coffee Place'").get()
            if not self._test_location:        
                models.Difficulty.load()    
                self._test_location = models.Location(owner=models.User.get_test_user(), location=db.GeoPt(37, -122))
                self._test_location.address = "310 SW 3rd Street"
                self._test_location.name = "Some Coffee Place"
                self._test_location.description = "Some Coffee Place used for testing"
                self._test_location.category = models.Category.gql("WHERE name='Coffee House'").get()
                self._test_location.city = "Corvallis"
                self._test_location.state = "OR"
                self._test_location.zip = "97333"
                self._test_location.county = "USA"
                self._test_location.phone_number = "(541) 757-3025"
                self._test_location.rating = 48
                self._test_location.put()
                gmaps = GoogleMaps(self.enviroment.google_maps_key)   
                lat, lng = gmaps.address_to_latlng(self._test_location.get_address())
                self._test_location.update_location(db.GeoPt(lat, lng))
                self._test_location.save()
                
                difficulty = models.Difficulty.find( 'MEDIUM' )
                oneMonth = datetime.timedelta(days=30)
                today = datetime.date.today()
                expires = today + oneMonth
            
                for i in range(1, 5):
                    code = self._create_item_code()
                    item = models.Item.get_by_key_name(code)
                    while item:
                        code = self._create_item_code()
                        item = models.Item.get_by_key_name(code)
                    item = models.Item(key_name=code, location=self._test_location,
                                       name="Free 20oz Coffee", 
                                       details="Show Code at Counter to get a Free Coffee 20oz", 
                                       active=True, code=code, expires=expires, difficulty=difficulty)
                    item.put()
            return self._test_location 
    

class Index(TestBase):
    
    url = "/test"
    url2 = "/test/"
    template = "index.html"
    
    def post(self):
        cleared_message = self.request.get('cleared_message')
        u = models.User.get_test_user()
        if u:
            at = models.AccessToken.create_for_user(u)
        self.generate(Index.template, {'cleared_message':cleared_message, 'test_user_access_token':at.token, 'test_location_key':self.test_location.key()})
        
    
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