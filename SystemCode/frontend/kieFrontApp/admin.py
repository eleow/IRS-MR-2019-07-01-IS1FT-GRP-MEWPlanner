from django.contrib import admin
from django.contrib.auth import get_user_model
from django.contrib.auth.admin import UserAdmin

from .forms import NewUserForm, UserUpdateForm
from .models import User

# Register your models here.
class CustomUserAdmin(UserAdmin):
    add_form = NewUserForm
    form = UserUpdateForm
    model = User 
    list_display = ['username', 'email', 'gender', 'height', 'weight', 'birth_date', 'activity',]

admin.site.register(User, CustomUserAdmin)
