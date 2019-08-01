from django import forms
from django.contrib.auth.forms import UserCreationForm, UserChangeForm, AuthenticationForm
# from django.contrib.auth.models import User
from django.contrib.auth import get_user_model
User = get_user_model()
import datetime

THIS_YEAR = datetime.datetime.now().year
BIRTH_YEAR_CHOICES =  list(range(THIS_YEAR-60, THIS_YEAR-18))
# PLACEHOLDER_DICT = {
#     'username': 'Your username',
#     'password1': 'Password',
#     'password2': 'Repeat your password',
#     'weight': "Weight (kg)",
#     'height': "Height (cm)",
# }

class NewUserForm(UserCreationForm):
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
    ACTIVITY_HELP = "Sedentary (Light or no exercise), "\
        "Light (Exercise 1-3 times/week), Moderate (Exercise 4-5 times/week), " \
        "Active (Daily Exercise or intense exercise 3-4 times/week), " \
        "Very Active (Intense Exercise 6-7 times/week), Extra Active (Very intense Exercise daily or physical job)"


    # Fields for calculation of BMI and Basal Metabolic Rate
    weight = forms.DecimalField(required = True, min_value = 20, max_value = 200, decimal_places = 0) #, help_text = "Please enter a weight 20 to 200 (kg)")
    height = forms.DecimalField(required = True, min_value = 50, max_value = 300, decimal_places = 0) #, help_text = "Please enter a height 50 to 300 (cm)")
    # birth_date = forms.DateField(required=True, widget=forms.SelectDateWidget(years=BIRTH_YEAR_CHOICES), help_text="Required. Format: DD-MM-YYYY", initial=datetime.date.today)
    birth_date = forms.DateField(required = True, widget = forms.SelectDateWidget(years = BIRTH_YEAR_CHOICES), initial = datetime.date(year=THIS_YEAR-18, month=1, day=1))
    gender = forms.ChoiceField(required = True, choices = [(0, 'Male'), (1, 'Female',)])
    activity = forms.ChoiceField(required = True, choices = ACTIVITY_CHOICES, help_text = ACTIVITY_HELP)

    # optional fields
    email = forms.EmailField(required=False, max_length=254) #, help_text='Please provide a valid email address.')

    def __init__(self, *args, **kwargs):
        super(NewUserForm, self).__init__(*args, **kwargs)
        for visible in self.visible_fields():
            # visible.field.widget.attrs['placeholder'] = PLACEHOLDER_DICT.get(visible.name, "")
            if (visible.name == "birth_date"): 
               visible.field.widget.attrs['class'] = 'form-input-date'
            else:
               visible.field.widget.attrs['class'] = 'form-input'
            # if (visible.field.required):
            #    visible.field.widget.attrs['class'] = 'required'
        # self.fields['email'].widget.attrs['placeholder'] = self.fields['email'].label or 'email@address.nl'
    
    class Meta:
        model = User
        fields = ('username', 'password1', 'password2', 'email', 'weight', 'height', 'gender', 'birth_date', 'activity')


class UserUpdateForm(UserChangeForm):
     class Meta:
        model = User
        fields = ('weight', 'height', 'activity')