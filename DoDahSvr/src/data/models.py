from google.appengine.ext import db
from google.appengine.ext.db import polymodel
import os
import datetime
import pytz
import hashlib
import logging
import math
import random
from geo import geomodel


class Pacific_tzinfo(datetime.tzinfo):
    """Implementation of the Pacific timezone."""
    def utcoffset(self, dt):
        return datetime.timedelta(hours=-8) + self.dst(dt)

    def _FirstSunday(self, dt):
        """First Sunday on or after dt."""
        return dt + datetime.timedelta(days=(6-dt.weekday()))

    def dst(self, dt):
        # 2 am on the second Sunday in March
        dst_start = self._FirstSunday(datetime.datetime(dt.year, 3, 8, 2))
        # 1 am on the first Sunday in November
        dst_end = self._FirstSunday(datetime.datetime(dt.year, 11, 1, 1))

        if dst_start <= dt.replace(tzinfo=None) < dst_end:
            return datetime.timedelta(hours=1)
        else:
            return datetime.timedelta(hours=0)
        
    def tzname(self, dt):
        if self.dst(dt) == datetime.timedelta(hours=0):
            return "PST"
        else:
            return "PDT"
        


class Environment(db.Model):    
    created = db.DateTimeProperty(auto_now_add=True)
    updated = db.DateTimeProperty(auto_now=True)
    server = db.StringProperty(required=True) 
    appid = db.StringProperty()
    facebook_api_key = db.StringProperty()
    facebook_secret_key = db.StringProperty()
    external_url = db.StringProperty()
    site_name = db.StringProperty()
    system_time_zone = db.StringProperty(default='US/Pacific')
    cookie_secret = db.StringProperty()
    email_from = db.StringProperty()
    google_maps_key = db.StringProperty()
     
    @classmethod
    def create(cls, 
               server, 
               appid, 
               facebook_api_key, 
               facebook_secret_key, 
               external_url, 
               site_name, 
               cookie_secret,
               email_from,
               maps_key):
        env = Environment(
                          server=server,
                          appid=appid,
                          facebook_api_key=facebook_api_key,
                          facebook_secret_key=facebook_secret_key,
                          external_url=external_url,
                          site_name=site_name,
                          cookie_secret=cookie_secret,
                          email_from=email_from,
                          google_maps_key=maps_key)
        env.put()
        return env
    
    @classmethod
    def load(cls):
        for e in Environment.all():
            e.delete()
        qa = Environment.create("glyphr", "140224239334183", "99769141121c38b2d30aeac4493da4ef", "ada9f63fc88f109319b09e497f906d8d", "http://glyphr.appspot.com", site_name="DoDah", cookie_secret='A18A0990-8B87-11DF-857E-2AE6DFD72085E885076E-8B87-11DF-9B3C-78E6DFD72085', email_from='noreply@dodah.com', maps_key="ABQIAAAAqJ8cnMoiSiWcwMV1Z0Sy0xR4EqRj3RNZnoYuzojShxUjcPQKRRRIg5eMJehIfcuV")    
        localhost = Environment.create("localhost", "266627909278", "c05d825b7ea4e060350658421c322070", "c1a12b8e45a63d5c764601b63caa113a", "http://localhost:8080", site_name="DoDah", cookie_secret='B1DEAEAE-8B87-11DF-AC7F-2CE6DFD72085F7D71C52-8B87-11DF-BF0C-7EE6DFD72085', email_from='noreply@dodah.com', maps_key="ABQIAAAAqJ8cnMoiSiWcwMV1Z0Sy0xR4EqRj3RNZnoYuzojShxUjcPQKRRRIg5eMJehIfcuV")

    @classmethod
    def get_for_current_environment(cls, server=None):
        if server is None:
            server = os.environ.get("APPLICATION_ID")
        host = os.environ.get("SERVER_NAME")
        if host == "localhost":
            server = host
        toReturn = Environment.gql("WHERE server=:svr", svr=server).get()
        if toReturn is None:
            if Environment.all().count() <= 0:
                Environment.load()
                return Environment.gql("WHERE server=:svr", svr=server).get()
        return toReturn

# Beta program
#
class BetaCode(db.Model):

    created = db.DateTimeProperty(auto_now_add=True)
    updated = db.DateTimeProperty(auto_now=True)
    name = db.StringProperty()
    code = db.StringProperty()
    email = db.EmailProperty()
    lastAccess = db.DateTimeProperty()
    
    @classmethod
    def create(cls, name, email):
        bcode = BetaCode.gql("WHERE email=:1", email).get()
        if not bcode:
            alphabet = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'
            code = "".join(random.sample(alphabet,8))
            bcode = BetaCode.get_by_key_name(code)
            while bcode:
                code = "".join(random.sample(alphabet,8))
                bcode = BetaCode.get_by_key_name(code)
            bcode = BetaCode(key_name=code, name=name, email=email)
            bcode.put()
        return bcode
    
    @classmethod
    def valid(cls, code, email):
        bcode = BetaCode.get_by_key_name(code)
        return (bcode is not None and bcode.email==email)
       
class User(db.Model):
    created = db.DateTimeProperty(auto_now_add=True)
    updated = db.DateTimeProperty(auto_now=True)
    first_name = db.StringProperty()
    last_name = db.StringProperty()
    email_address = db.EmailProperty()
    password = db.StringProperty()
    fb_uid = db.StringProperty()
    fb_profile_url = db.StringProperty()
    fb_access_token = db.StringProperty()
    twitter_uname = db.StringProperty()
    address = db.StringProperty()
    city = db.StringProperty()
    state = db.StringProperty()
    zip = db.StringProperty()
    country = db.StringProperty()
    phone_number = db.StringProperty() 
    newsletter = db.BooleanProperty()
    thirdparty = db.BooleanProperty()
    
    @classmethod
    def create(cls, first_name, last_name, email, password):
        u = User(key_name=email, first_name=first_name, last_name=last_name, email_address=email, password=password)
        u.put()
        return u
        
    @classmethod
    def email_exists(cls, email):
        user = User.get_by_key_name(email)
        if user:
            return True
        else:
            return False
    
    @classmethod 
    def get_test_user(cls):
        testuser = User.get_by_key_name("test@example.com")
        if not testuser:
            testuser = User(key_name=str("test@example.com"),
                                fb_uid=str(999999),
                                first_name="Testy",
                                last_name="McTestington",
                                profile_url="wwww.facebook.com",
                                access_token="no available")
            testuser.address = "123 Fake St"
            testuser.email_address = "test@example.com"
            testuser.city = "New York"
            testuser.state = "NY"
            testuser.zip = "10107"
            testuser.country = "US"
            testuser.phonenumber = "18005551212"
            testuser.newsletter = True
            testuser.thirdparty = False
            testuser.customerProfileId = None
            testuser.password=hashlib.md5("salsa").hexdigest()
            testuser.put()
            
        return testuser
    
    @property
    def name(self):
    
        if not self.first_name:
            self.first_name = ""
        if not self.last_name:
            self.last_name = ""
        
        if self.first_name == "" and self.last_name == "":
            return self.fullname
            
        return "%s %s" % (self.first_name, self.last_name)
    
# Access Token
#
class AccessToken(db.Model):

    created = db.DateTimeProperty(auto_now_add=True)
    updated = db.DateTimeProperty(auto_now=True)
    name = db.StringProperty()
    token = db.StringProperty()
    user = db.ReferenceProperty(User, required=True)
    valid_days = db.IntegerProperty(default=30)
    expired = db.BooleanProperty(default=False)
    
    @classmethod
    def _create_token(self):
        alphabet = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'
        return "".join(random.sample(alphabet,15))
    
    @classmethod
    def create_for_user(cls, user):
        if user:
            token = AccessToken.find_for_user(user)
            if token:
                return token
            else:
                token_str = AccessToken._create_token()
                token = AccessToken.get_by_key_name(token_str)
                while token:
                    token_str = AccessToken._create_token()
                    token = AccessToken.get_by_key_name(token_str)
                token = AccessToken(key_name=token_str, token=token_str, user=user)
                token.save()
                return token
    
    @classmethod
    def valid(cls, token):
        token = AccessToken.get_by_key_name(token)
        if token and not token.expired:
            timedelta = datetime.datetime.now() - token.created
            if timedelta.days > token.valid_days:
                token.expired = True
                token.save()
            else:
                return token            
        return None
    
    @classmethod
    def find_for_user(cls, user):
        return AccessToken.gql("WHERE user=:1 and expired=:2", user, False).get()

class ServiceClient(db.Model):
    created = db.DateTimeProperty(auto_now_add=True)
    updated = db.DateTimeProperty(auto_now=True)
    name = db.StringProperty(required=True)
    client_key = db.StringProperty()
    
    @classmethod
    def load(cls):
        for e in ServiceClient.all():
            e.delete()
        android = ServiceClient(key_name="B902A730A4D211DFA5C53432E0D72085", name="android", client_key="B902A730A4D211DFA5C53432E0D72085")  
        android.put()
        webtest = ServiceClient(key_name="882AF324A4DC11DFB3BABF3DE0D72085", name="webtest", client_key="882AF324A4DC11DFB3BABF3DE0D72085")  
        webtest.put()
        
    @classmethod
    def valid(cls, key):  
        if ServiceClient.all().count() <= 0:
            ServiceClient.load()
        return ServiceClient.get_by_key_name(key)
    
class ServiceLog(db.Model):
    created = db.DateTimeProperty(auto_now_add=True)
    updated = db.DateTimeProperty(auto_now=True)
    call = db.StringProperty()
    service_client = db.ReferenceProperty(ServiceClient, required=True, collection_name="log")
    access_token = db.ReferenceProperty(AccessToken, collection_name="log")
    
    @classmethod
    def create(cls, call, client, token):
        log = ServiceLog(call=call,service_client=client, access_token=token )
        log.put()
        return log
        

class Category(db.Model):
    created = db.DateTimeProperty(auto_now_add=True)
    updated = db.DateTimeProperty(auto_now=True)
    name = db.StringProperty(required=True)
    description = db.TextProperty()
    parent_category = db.SelfReferenceProperty(collection_name='sub_categories')
    
    def __str__(self):
        return self.name
    
class Location(geomodel.GeoModel):
    created = db.DateTimeProperty(auto_now_add=True)
    updated = db.DateTimeProperty(auto_now=True)
    owner = db.ReferenceProperty(User)
    name = db.StringProperty()
    category = db.ReferenceProperty(Category)
    description = db.TextProperty()
    address = db.StringProperty()
    city = db.StringProperty()
    state = db.StringProperty()
    zip = db.StringProperty()
    country = db.StringProperty()
    phone_number = db.StringProperty()
    rating = db.RatingProperty()
    
    def get_address(self):
        return "%s %s, %s %s" % ( self.address, self.city, self.state, self.zip )
    
    def get_thumbnail_url(self):
        if self.location:
            return "http://maps.google.com/maps/api/staticmap?center=%s,%s&size=200x200&sensor=false&zoom=17&&maptype=roadmap&markers=color:blue|label:X|%s,%s" % ( self.location.lat, self.location.lon, self.location.lat, self.location.lon )
    
class Image(db.Model):
    created = db.DateTimeProperty(auto_now_add=True)
    updated = db.DateTimeProperty(auto_now=True)
    location = db.ReferenceProperty(Location, required=True, collection_name='images')
    blob_key = db.StringProperty(required=True) 
    
class Difficulty(db.Model):
    created = db.DateTimeProperty(auto_now_add=True)
    updated = db.DateTimeProperty(auto_now=True)
    name = db.StringProperty(required=True)
    time_to_find_seconds = db.FloatProperty(required=True, default=0.25)
    
    @classmethod
    def load(cls):
        if Difficulty.all().count() <= 0:
            Difficulty(name='INSTANT', time_to_find_seconds=0.25).put()
            Difficulty(name='SIMPLE', time_to_find_seconds=5.0).put()
            Difficulty(name='EASY', time_to_find_seconds=10.0).put()
            Difficulty(name='MEDIUM', time_to_find_seconds=45.0).put()
            Difficulty(name='HARD', time_to_find_seconds=120.0).put()
            Difficulty(name='ARDUOUS', time_to_find_seconds=300.0).put()
            Difficulty(name='IMPOSSIBLE', time_to_find_seconds=3600.0).put()
        
    @classmethod
    def find(cls, name):
        return Difficulty.gql("WHERE name=:1", name).get()
    
    def __str__(self):
        return self.name.capitalize()
    
class Item(db.Model):
    created = db.DateTimeProperty(auto_now_add=True)
    updated = db.DateTimeProperty(auto_now=True)
    location = db.ReferenceProperty(Location, required=True, collection_name='items')
    code = db.StringProperty(required=True)
    difficulty = db.ReferenceProperty(Difficulty)
    name = db.StringProperty(default="")
    details = db.TextProperty(default="")
    found_user = db.ReferenceProperty(User, collection_name='found_items')
    redeemed = db.BooleanProperty(default=False)
    expires = db.DateProperty()
    active = db.BooleanProperty(default=False)
    
    
            