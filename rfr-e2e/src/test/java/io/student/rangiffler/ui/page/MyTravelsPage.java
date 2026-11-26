package io.student.rangiffler.ui.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.ui.component.HeaderComponent;
import io.student.rangiffler.ui.component.PhotoCard;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class MyTravelsPage {
  public static final String URL = TestConfig.frontUrl() + "/my-travels";
  private final Page page;
  private final HeaderComponent header;
  private final Locator travelsMap;
  private final Locator photos;
  private final Locator onlyMine;
  private final Locator withFriends;
  private final Locator addPhoto;
  private final Locator photoCards;

  public MyTravelsPage(Page page) {
    this.page = page;
    this.header = new HeaderComponent(page);
    this.travelsMap = page.getByRole(AriaRole.HEADING,
        new Page.GetByRoleOptions().setName("Travels map").setExact(true));
    this.photos = page.getByRole(AriaRole.HEADING,
        new Page.GetByRoleOptions().setName("Photos").setExact(true));
    this.onlyMine = page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Only my travels").setExact(true));
    this.withFriends = page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("With friends").setExact(true));
    this.addPhoto = page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Add photo").setExact(true));
    this.photoCards = page.getByRole(AriaRole.ARTICLE);
  }

  public PhotoCard photoCard(String description) {
    return new PhotoCard(page, photoCards.filter(new Locator.FilterOptions().setHasText(description)),
        description);
  }

  @Step("Проверить основные элементы страницы My travels")
  public MyTravelsPage checkThatMainElementsVisible() {
    assertThat(travelsMap).isVisible();
    assertThat(photos).isVisible();
    assertThat(onlyMine).isVisible();
    assertThat(onlyMine).hasAttribute("aria-pressed", "true");
    assertThat(withFriends).isVisible();
    assertThat(addPhoto).isVisible();
    header.checkThatVisible();
    return this;
  }

  @Step("Проверить число фотокарточек: {count}")
  public MyTravelsPage checkThatPhotosCount(int count) {
    assertThat(photoCards).hasCount(count);
    return this;
  }

  @Step("Проверить счётчик посещённых стран: {label}")
  public MyTravelsPage checkThatVisitedCountriesLabel(String label) {
    assertThat(page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName(label).setExact(true))).isVisible();
    return this;
  }

  @Step("Открыть список посещённых стран по подписи: {label}")
  public MyTravelsPage openVisitedCountries(String label) {
    page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName(label).setExact(true)).click();
    return this;
  }

  @Step("Выбрать страну в списке посещённых: {country}")
  public MyTravelsPage selectVisitedCountry(String country) {
    page.getByRole(AriaRole.TOOLTIP)
        .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(country).setExact(true))
        .click();
    return this;
  }

  @Step("Проверить чип фильтра страны: {country}")
  public MyTravelsPage checkThatCountryFilter(String country) {
    assertThat(page.getByLabel("Country filter: " + country,
        new Page.GetByLabelOptions().setExact(true))).isVisible();
    return this;
  }

  @Step("Сбросить фильтр страны: {country}")
  public MyTravelsPage resetCountryFilter(String country) {
    page.getByLabel("Country filter: " + country, new Page.GetByLabelOptions().setExact(true))
        .getByRole(AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName("Reset country filter").setExact(true))
        .click();
    return this;
  }

  @Step("Открыть People")
  public PeoplePage clickPeople() {
    return header.clickPeople();
  }

  @Step("Открыть профиль")
  public ProfilePage clickProfile() {
    return header.clickProfile();
  }
}
