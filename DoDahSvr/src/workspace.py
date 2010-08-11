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
import base
     
class Login(base.Base):
    
    url = "/login"
    template = "login.html"
    
    def post(self):        
        cookieutil = lilcookies.LilCookies(self, self.enviroment.cookie_secret )
        cookieutil.clear_all_cookies()
        email = self.param('email')
        password = self.param('password')    
        if email and password:    
            passw = hashlib.md5(password).hexdigest()
            user = models.User.get_by_key_name(email)
            if user and user.password == passw:
                cookieutil = lilcookies.LilCookies(self, self.enviroment.cookie_secret )
                cookieutil.set_secure_cookie(name = 'uid', value=str(user.key()), expires_days=365)
                self.redirect(Index.url)
            else:
                self.generate( Login.template, { 'message':"unable to log in" } )
        else:
                self.generate( Login.template, None )
                
class Logout(base.Base):
    
    url = "/logout"
    
    def do_post(self):
        cookieutil = lilcookies.LilCookies(self, self.enviroment.cookie_secret )
        cookieutil.clear_all_cookies()
        self.redirect(Login.url)
            
class Locations(base.Base):
    
    url = "/locations"
    template = "locations.html"
    
    def do_post(self):        
        self.generate(Locations.template, { 'locations':models.Location.all() })

class LocationForm(djangoforms.ModelForm):
    class Meta:
        model = models.Location
        exclude = ( 'rating', 'location','location_geocells', 'items', 'updated', 'created', 'meta', '_class', 'create_ts', 'modify_ts', 'edited_by',)
        category = forms.ModelChoiceField(queryset=models.Category.all())
        description = forms.CharField(widget=forms.Textarea(attrs={'cols': 130, 'rows': 20}))
        
class EditLocation(base.Base):
    
    url = "/editlocation"
    template = "editlocation.html"
    
    @classmethod
    def get_url(cls, itemId):
        return EditLocation.url + "?id=" + str(itemId)
    
    def do_post(self):
        id = self.request.get('id')
        
        locForm = None
        loc = None
       
        if id and id != "":
            loc = db.get(id)
            locForm = LocationForm(instance=loc)
        else:
            locForm = LocationForm()
        template_values = {
                           'loc':loc,
                           'form':locForm,
                           'diffs':models.Difficulty.all()
                           }
        self.generate(EditLocation.template, template_values)
        
class SaveLocation(base.Base):
    url = "/savelocation"
    
    def do_post(self):
        id = self.param('id')
        entity = None
        if id and id != "":
            locForm = LocationForm(data=self.request.POST, instance=db.get(id))
        else:
            locForm = LocationForm(data=self.request.POST)        
        if locForm.is_valid():
            entity = locForm.save(commit=False)
            entity.owner = self.current_user
            entity.save() 
            id = entity.key()
            gmaps = GoogleMaps(self.enviroment.google_maps_key)   
            lat, lng = gmaps.address_to_latlng(entity.get_address())
            entity.update_location(db.GeoPt(lat, lng))
            entity.save()
        
        template_values = {
                               'loc':entity,
                               'form':locForm
                               }
        self.generate(EditLocation.template, template_values)
        
class SaveItem(base.Base):
    url = "/saveitem"
    
    def do_post(self):
        id = self.param('id')
        locid = self.param('locid')
        name = self.param('name')
        details = self.param('details')
        difficulty = self.param('difficulty')
        active = ( self.param('active') == 'yes' )
        item = db.get(id)
        if item:
            diff = models.Difficulty.find(difficulty)
            item.name = name
            item.details = details
            item.difficulty = diff
            item.active = active
            item.save()
        self.redirect(EditLocation.get_url(locid))
        
class GenerateItems(base.Base):
    
    url="/generate"
    
    def do_post(self):
        id = self.param('id')
        number = self.param('number')
        num = 5
        loc = db.get(id)
        diff = models.Difficulty.find('EASY')
        if loc:
            oneMonth = datetime.timedelta(days=30)
            today = datetime.date.today()
            expires = today + oneMonth
            if number:
                num = int(number)
            for i in range(1, int(num)):
                code = self._create_item_code()
                item = models.Item.get_by_key_name(code)
                while item:
                    code = self._create_item_code()
                    item = models.Item.get_by_key_name(code)
                item = models.Item(key_name=code, location=loc, code=code, expires=expires, difficulty=diff)
                item.put()
        self.redirect(EditLocation.get_url(id))
        
class FindLocations(base.Base):
    
    url = "/findlocations"
    template_name = "findlocations.html"
    
    def do_post(self):
        
        address = self.param('address')
        miles = self.param('miles')
        max_results = self.param('max_results')
        if not max_results:
            max_results = 10
        values = { 'address':address, 'max_results':max_results,'miles':miles }
        if address and miles:
            gmaps = GoogleMaps(self.enviroment.google_maps_key)   
            lat, lng = gmaps.address_to_latlng(address)
            geoPoint = db.GeoPt(lat, lng)
            meters = float(miles) * base.METERS_IN_MILE        
            locations = models.Location.proximity_fetch( models.Location.all(), geoPoint, int(max_results), meters )
            values['locations'] = locations
        
        self.generate(FindLocations.template_name, values)
        
class SignUp(base.Base):
    
    url = "/signup"
    template_name = "signup.html"
    
    def post(self):
        betaCode = self.request.get('betacode')
        first_name = self.request.get('first_name')
        last_name = self.request.get('last_name')
        emailAddress = self.request.get('email')
        password = self.request.get('password')
        message = ""
        skip = ( emailAddress == "tim.romanowski@gmail.com" )
        
        if skip or betaCode and emailAddress and password:
            if models.User.email_exists(emailAddress):
                message = "Email Address is already in use"
            elif skip == False and models.BetaCode.valid(betaCode, emailAddress):
                message = "Either beta code is incorrect or email address does not match the code email."
            else:
                passw = hashlib.md5(password).hexdigest()
                user = models.User.create(first_name, last_name, emailAddress, passw)
                if user:
                    self.redirect(Index.url)
                else:
                    message = "Unable to create account"
            
        values = {'betacode':betaCode, 'first_name':first_name,'last_name':last_name, 'email':emailAddress, 'message':message}
        self.generate(SignUp.template_name, values)
        
        
class Profile(base.Base):
    
    url = "/profile"
    template_name = "profile.html"
    
    def do_post(self):
        id = self.request.get('xjf')
        if id:
            self.current_user.first_name = self.request.get('first_name')
            self.current_user.last_name = self.request.get('last_name')
            self.current_user.email_address = self.request.get('email')
            self.current_user.address = self.request.get('address')
            self.current_user.city = self.request.get('city')
            self.current_user.state = self.request.get('state')
            self.current_user.zip = self.request.get('zip')
            self.current_user.country = self.request.get('country')
            self.current_user.phone_number = self.request.get('phone_number')
            newsletter = self.request.get('newsletter')
            thirdparty = self.request.get('thirdparty')
            if newsletter:
                self.current_user.newsletter = True
            else:
                self.current_user.newsletter = False
            
            if thirdparty:
                self.current_user.thirdparty = True
            else:
                self.current_user.thirdparty = False;
            self.current_user.save()
        
        self.update_current_user_facebook()
        self.generate(Profile.template_name, None)
        
class About(base.Base):
    
    url = "/about"
    template_name = "about.html"
    
    def post(self):
        self.generate(About.template_name, None)
        
class Privacy(base.Base):
    
    url = "/privacy"
    template_name = "privacy.html"
    
    def post(self):
        self.generate(Privacy.template_name, None)
        
class HowItWorks(base.Base):
    
    url = "/howitworks"
    template_name = "howitworks.html"
    
    def post(self):
        self.generate(HowItWorks.template_name, None)
        
class ContactUs(base.Base):
    
    url = "/contact"
    template_name = "contactus.html"
    
    def post(self):
        self.generate(ContactUs.template_name, None)
        
class Terms(base.Base):
    
    url = "/terms"
    template_name = "terms.html"
    
    def post(self):
        self.generate(Terms.template_name, None)
        
        

class Index(base.Base):
    
    url = "/"    
    template_name = "index.html"
    
    def post(self):
        self.generate(Index.template_name, None)
        
class LoadTestData(base.Base):
    
    url = "/loadtestdata"
    
    def post(self):
        
        models.Difficulty.load()
        
        models.User.get_test_user()
        self.redirect(Index.url)
        
        cat = models.Category(name="Food/Beverage", description="")
        cat.put()
        
        subcat = models.Category(name="Resturant", description="", parent_category=cat)
        subcat.put()
        
        subcat = models.Category(name="Coffee House", description="", parent_category=cat)
        subcat.put()
        
        red = models.Location(owner=self.current_user, location=db.GeoPt(37, -122))
        red.address = "310 SW 3rd Street"
        red.name = "Red Horse"
        red.description = "Red Horse Coffee"
        red.category = subcat
        red.city = "Corvallis"
        red.state = "OR"
        red.zip = "97333"
        red.county = "USER"
        red.phone_number = "(541) 757-3025"
        red.rating = 99
        red.put()
        gmaps = GoogleMaps(self.enviroment.google_maps_key)   
        lat, lng = gmaps.address_to_latlng(red.get_address())
        red.update_location(db.GeoPt(lat, lng))
        red.save()
        
        
        
        
        


application = webapp.WSGIApplication([
                                       (Index.url, Index),
                                       (Login.url, Login),
                                       (Logout.url,Logout),
                                       (LoadTestData.url, LoadTestData), 
                                       (Locations.url, Locations),
                                       (EditLocation.url, EditLocation),
                                       (SaveLocation.url, SaveLocation),
                                       (SaveItem.url,SaveItem),
                                       (GenerateItems.url, GenerateItems),
                                       (FindLocations.url,FindLocations),
                                       (Profile.url, Profile),
                                       (About.url, About),
                                       (HowItWorks.url, HowItWorks),
                                       (ContactUs.url, ContactUs),
                                       (Privacy.url, Privacy),
                                       (Terms.url, Terms),
                                       (SignUp.url, SignUp)                           
                                     ], debug=True)



def main():
    run_wsgi_app(application)

if __name__ == "__main__":
    main()

