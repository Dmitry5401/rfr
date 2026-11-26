package io.student.rangiffler.test.travels;

import com.microsoft.playwright.Page;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.UiTest;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.jupiter.extension.UserExtension;
import io.student.rangiffler.ui.page.LandingPage;
import io.student.rangiffler.ui.page.MyTravelsPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@UiTest
@ExtendWith(UserExtension.class)
@DisplayName("My travels на mock-ленте")
class MyTravelsMockTest {
  private static final int MINE_PHOTOS = 4;
  private static final int WITH_FRIENDS_PHOTOS = 7;
  private static final String MINE_COUNTRIES_LABEL = "4 countries visited";
  private static final String WITH_FRIENDS_COUNTRIES_LABEL = "7 countries visited";
  private static final String FILTER_COUNTRY = "Czech Republic";
  private static final String OWN_PHOTO = "Амстердам!";
  private static final String FRIEND_PHOTO = "Старая Прага завораживает!";

  @Test
  @DisplayName("Переключатели Only my travels / With friends меняют число карточек и счётчик стран на mock-ленте")
  void togglesChangeCardCountAndCountriesCounter(Page page, @User TestUser user) {
    openMyTravels(page, user)
        .checkThatPhotosCount(MINE_PHOTOS)
        .checkThatVisitedCountriesLabel(MINE_COUNTRIES_LABEL)
        .clickWithFriends()
        .checkThatPhotosCount(WITH_FRIENDS_PHOTOS)
        .checkThatVisitedCountriesLabel(WITH_FRIENDS_COUNTRIES_LABEL)
        .clickOnlyMyTravels()
        .checkThatPhotosCount(MINE_PHOTOS)
        .checkThatVisitedCountriesLabel(MINE_COUNTRIES_LABEL);
  }

  @Test
  @DisplayName("Фильтр страны на mock-ленте показывает чип и одну карточку, Reset возвращает полный список")
  void countryFilterShowsChipAndSingleCardThenResets(Page page, @User TestUser user) {
    MyTravelsPage travels = openMyTravels(page, user)
        .clickWithFriends()
        .checkThatPhotosCount(WITH_FRIENDS_PHOTOS)
        .openVisitedCountries(WITH_FRIENDS_COUNTRIES_LABEL)
        .selectVisitedCountry(FILTER_COUNTRY)
        .checkThatCountryFilter(FILTER_COUNTRY)
        .checkThatPhotosCount(1);
    travels.photoCard(FRIEND_PHOTO).checkThatVisible();
    travels.resetCountryFilter(FILTER_COUNTRY)
        .checkThatPhotosCount(WITH_FRIENDS_PHOTOS)
        .checkThatVisitedCountriesLabel(WITH_FRIENDS_COUNTRIES_LABEL);
  }

  @Test
  @DisplayName("Photo actions на mock-ленте есть только у своих фото и нет у фото друзей")
  void photoActionsOnlyOnOwnPhotos(Page page, @User TestUser user) {
    MyTravelsPage travels = openMyTravels(page, user).clickWithFriends();
    travels.photoCard(OWN_PHOTO).checkThatPhotoActionsVisible();
    travels.photoCard(FRIEND_PHOTO).checkThatPhotoActionsAbsent();
  }

  private MyTravelsPage openMyTravels(Page page, TestUser user) {
    return new LandingPage(page).open()
        .clickLogin()
        .fillLoginForm(user.username(), user.password())
        .clickSignIn();
  }
}
