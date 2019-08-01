(function ($) {

  $(".toggle-password").click(function () {

    $(this).toggleClass("zmdi-eye zmdi-eye-off");
    var $input = $($(this).attr("toggle1"));
    var $confirm_input = $($(this).attr("toggle2"));
    var $confirm_div = $confirm_input.parent();
    if ($input.attr("type") == "password") {
      $input.attr("type", "text");
      $confirm_input.attr("type", "text");
      $confirm_input.val($input.val());
      $confirm_div.hide();
    } else {
      $input.attr("type", "password");
      $confirm_input.attr("type", "password");
      $confirm_input.val("");
      $confirm_div.show();
    }
  });

  $('#submit_signup').submit(function () {
    $("#id_password2").show(); // need to show password2 in case it was hidden
    return true; // return false to cancel form action
  });

  function updateBMI() {
    let wt = $('#id_weight').val();
    let ht = $('#id_height').val();
    bmi = wt / (ht / 100) ** 2;
    $('#id_bmi').val(bmi.toFixed(2));

    let tip = "";
    let color = "grey";
    // update bmi hint message
    if (bmi <= 18.5) {
      tip = "Risk of nutritional deficiency diseases and osteoporosis";
      color = "red";
    } else if (bmi < 23.0) {
      tip = "Maintain your healthy weight by balancing diet and exercise";
      color = "grey";
    } else if (bmi < 27.5) {
      tip = "Aim to lose 5-10% of your weight over the next 6-12 months for a healthier you";
      color = "#ff9900";
    } else {
      tip = "You are at risk of numerous health problems. Aim to lose 5-10% of your weight over the next 6-12 months. Lose weight to stay healthy";
      color = "red";
    }
    $('#h_bmi').text(tip);
    $('#h_bmi').css("color", color);
  };

  function updateAMR() {
    let act_arr = [1.2, 1.375, 1.465, 1.55, 1.725, 1.9];
    let lose_weight_arr = [0, 0.1, 0.2, 0.4, 0.9]; // For safe weight loss, it is recommended that you lose no more than 1-2 pounds per week (0.45-0.9)

    let wt = $('#id_weight').val();
    let ht = $('#id_height').val();
    let gender = $('#id_gender').val();
    let age = $('#id_age').val();
    let act = act_arr[$('#id_activity').val()];
    let bmr = 0;
    let lose_weight = lose_weight_arr[$('#id_goals').val()];
    let min_cal = 0;

    if (gender === "Male") {
      bmr = 10 * wt + 6.25 * ht - 5 * age + 5;
      min_cal = 1500;
    } else {
      bmr = 10 * wt + 6.25 * ht - 5 * age - 161;
      min_cal = 1200;
    }
    amr = act * bmr;

    // Harvard Health Publications suggests women get at least 1,200 calories 
    // and men get at least 1,500 calories a day unless supervised by doctors.
    amr = Math.max(min_cal, amr - lose_weight / 7 / 0.45 * 3500);

    // console.log(amr);
    $('#id_calories').val(Math.round(amr));

    if ((gender === "Male" && amr - 1500 <= 1) || (gender === "Female" && amr - 1200 <= 1)) {
      tip = "Harvard Health Publications suggests women get at least 1,200 calories and men get at least 1,500 calories a day unless supervised by doctors";
      $('#h_cal').text(tip);
    } else {
      $('#h_cal').text("");
    }
  };

  // createPlans - update calculated values on change
  $('#id_weight, #id_height').change(function () {
    updateBMI();
    updateAMR();
  })

  $('#id_activity, #id_goals').change(function () {
    updateAMR();
  })

  $('#createMeal-form').submit(function (e) {

    // Hide everything
    $('#createMeal-form').css("display", "none")

    // Show "Please Wait" loader
    $(".signup-content")
      .append("<h1 class=\"loader\">Please Wait</h1><img class=\"loader\" alt=\"Please Wait\" src=\"/static/images/loader-2_food.gif\">");
    
    amr = $('#id_calories').val();

    return true;
  });

  $("input[type=number].form-input").keypress(function () {
    return (event.charCode == 8 || event.charCode == 0 || event.charCode == 13) ? // handle delete, backspace, enter
      null : event.charCode >= 48 && event.charCode <= 57; // restrict input to only 0-9
  });

  $("form.signup-form").validate({
    onfocusout: function (e) {
      $(e).valid();
    }
  });



  /*==================================================================
  [ Simple slide100 ]*/
  $('.simpleslide100').each(function () {
    var delay = 7000;
    var speed = 1000;
    var itemSlide = $(this).find('.simpleslide100-item');
    var nowSlide = 0;

    $(itemSlide).hide();
    $(itemSlide[nowSlide]).show();
    nowSlide++;
    if (nowSlide >= itemSlide.length) {
      nowSlide = 0;
    }

    setInterval(function () {
      $(itemSlide).fadeOut(speed);
      $(itemSlide[nowSlide]).fadeIn(speed);
      nowSlide++;
      if (nowSlide >= itemSlide.length) {
        nowSlide = 0;
      }
    }, delay);

    /*==================================================================
    [ Validate after type ]*/
    $('.validate-input .input100').each(function () {
      $(this).on('blur', function () {
        if (validate(this) == false) {
          showValidate(this);
        } else {
          $(this).parent().addClass('true-validate');
        }
      })
    })


    /*==================================================================
    [ Validate ]*/
    var input = $('.validate-input .input100');

    $('.validate-form').on('submit', function () {
      var check = true;

      for (var i = 0; i < input.length; i++) {
        if (validate(input[i]) == false) {
          showValidate(input[i]);
          check = false;
        }
      }

      return check;
    });


    $('.validate-form .input100').each(function () {
      $(this).focus(function () {
        hideValidate(this);
        $(this).parent().removeClass('true-validate');
      });
    });

    function validate(input) {
      if ($(input).attr('type') == 'email' || $(input).attr('name') == 'email') {
        if ($(input).val().trim().match(/^([a-zA-Z0-9_\-\.]+)@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.)|(([a-zA-Z0-9\-]+\.)+))([a-zA-Z]{1,5}|[0-9]{1,3})(\]?)$/) == null) {
          return false;
        }
      } else {
        if ($(input).val().trim() == '') {
          return false;
        }
      }
    }

    function showValidate(input) {
      var thisAlert = $(input).parent();

      $(thisAlert).addClass('alert-validate');

      $(thisAlert).append('<span class="btn-hide-validate">&#xf135;</span>')
      $('.btn-hide-validate').each(function () {
        $(this).on('click', function () {
          hideValidate(this);
        });
      });
    }

    function hideValidate(input) {
      var thisAlert = $(input).parent();
      $(thisAlert).removeClass('alert-validate');
      $(thisAlert).find('.btn-hide-validate').remove();
    }

  });

  // colorlib-wizard-8
  $("#wizard").steps({
    headerTag: "h4",
    bodyTag: "section",
    transitionEffect: "fade",
    enableAllSteps: true,
    transitionEffectSpeed: 300,
    labels: {
      next: "Continue",
      previous: "Back",
      finish: 'Proceed to checkout'
    },
    onStepChanging: function (event, currentIndex, newIndex) {
      if (newIndex >= 1) {
        $('.steps ul li:first-child a img').attr('src', 'images/step-1.png');
      } else {
        $('.steps ul li:first-child a img').attr('src', 'images/step-1-active.png');
      }

      if (newIndex === 1) {
        $('.steps ul li:nth-child(2) a img').attr('src', 'images/step-2-active.png');
      } else {
        $('.steps ul li:nth-child(2) a img').attr('src', 'images/step-2.png');
      }

      if (newIndex === 2) {
        $('.steps ul li:nth-child(3) a img').attr('src', 'images/step-3-active.png');
      } else {
        $('.steps ul li:nth-child(3) a img').attr('src', 'images/step-3.png');
      }

      if (newIndex === 3) {
        $('.steps ul li:nth-child(4) a img').attr('src', 'images/step-4-active.png');
        $('.actions ul').addClass('step-4');
      } else {
        $('.steps ul li:nth-child(4) a img').attr('src', 'images/step-4.png');
        $('.actions ul').removeClass('step-4');
      }
      return true;
    }
  });
  // Custom Button Jquery Steps
  $('.forward').click(function () {
    $("#wizard").steps('next');
  })
  $('.backward').click(function () {
    $("#wizard").steps('previous');
  })
  // Click to see password 
  $('.password i').click(function () {
    if ($('.password input').attr('type') === 'password') {
      $(this).next().attr('type', 'text');
    } else {
      $('.password input').attr('type', 'password');
    }
  })
  // Create Steps Image
  $('.steps ul li:first-child').append('<img src="images/step-arrow.png" alt="" class="step-arrow">').find('a').append('<img src="images/step-1-active.png" alt=""> ').append('<span class="step-order">Step 01</span>');
  $('.steps ul li:nth-child(2').append('<img src="images/step-arrow.png" alt="" class="step-arrow">').find('a').append('<img src="images/step-2.png" alt="">').append('<span class="step-order">Step 02</span>');
  $('.steps ul li:nth-child(3)').append('<img src="images/step-arrow.png" alt="" class="step-arrow">').find('a').append('<img src="images/step-3.png" alt="">').append('<span class="step-order">Step 03</span>');
  $('.steps ul li:last-child a').append('<img src="images/step-4.png" alt="">').append('<span class="step-order">Step 04</span>');
  // Count input 
  $(".quantity span").on("click", function () {

    var $button = $(this);
    var oldValue = $button.parent().find("input").val();

    if ($button.hasClass('plus')) {
      var newVal = parseFloat(oldValue) + 1;
    } else {
      // Don't allow decrementing below zero
      if (oldValue > 0) {
        var newVal = parseFloat(oldValue) - 1;
      } else {
        newVal = 0;
      }
    }
    $button.parent().find("input").val(newVal);
  });

})(jQuery);