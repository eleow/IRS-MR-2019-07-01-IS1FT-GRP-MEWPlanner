from django.db import models

from django.db import models
from django.contrib.auth.models import AbstractUser



class User(AbstractUser):
    GENDER_MALE = 0
    GENDER_FEMALE = 1
    GENDER_CHOICES = [(GENDER_MALE, 'Male'), (GENDER_FEMALE, 'Female')]

    ACT_SEDENTARY = 0
    ACT_LIGHT = 1
    ACT_MODERATE = 2
    ACT_ACTIVE= 3
    ACT_VERYACTIVE= 4
    ACT_EXTRAACTIVE= 5
    ACTIVITY_CHOICES = [(ACT_SEDENTARY, 'Sedentary'), (ACT_LIGHT, 'Light'), (ACT_MODERATE, 'Moderate'), 
        (ACT_ACTIVE, 'Active'), (ACT_VERYACTIVE, 'Very Active'), (ACT_EXTRAACTIVE, 'Extra Active')]

    CUISINE_CHOICES = [(0, 'none'), (1, 'chinese'), (2, 'malay'), (3, 'indian'), (4, 'western')]

    email = models.EmailField(null=True, blank=True)
    weight = models.DecimalField(null=True, blank=True, decimal_places=2, max_digits=6)
    height = models.DecimalField(null=True, blank=True, decimal_places=2, max_digits=6 )
    birth_date = models.DateField(null=True, blank=True)
    gender = models.IntegerField(null=True, default=0, blank=True, choices=GENDER_CHOICES)
    activity = models.IntegerField(null=True, default=0, blank=True, choices=ACTIVITY_CHOICES)
    cuisine_preference = models.IntegerField(null=True, default=0, blank=True, choices=CUISINE_CHOICES)
    takes_beef = models.IntegerField(null=True, default=0, blank=True, choices=[(0, 'yes'), (1, 'no')])

