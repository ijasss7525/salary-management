"""Records the demo video of the running app (http://localhost:8080) with on-screen captions.

Usage: python3 docs/demo/record_demo.py [chrome-executable]
Output: docs/demo/out/*.webm (convert to mp4 with ffmpeg).
"""
import sys
from playwright.sync_api import sync_playwright

BASE = "http://localhost:8080"
CHROME = sys.argv[1] if len(sys.argv) > 1 else None

CAPTION_JS = """
(text) => {
  let el = document.getElementById('demo-caption');
  if (!el) {
    el = document.createElement('div');
    el.id = 'demo-caption';
    el.style.cssText = 'position:fixed;left:50%;top:76px;transform:translateX(-50%);z-index:99999;' +
      'background:rgba(20,24,33,.88);color:#fff;padding:12px 22px;border-radius:12px;font:500 18px Segoe UI,Arial;' +
      'max-width:80%;text-align:center;box-shadow:0 6px 24px rgba(0,0,0,.25);pointer-events:none';
    document.body.appendChild(el);
  }
  el.textContent = text;
  el.style.display = text ? 'block' : 'none';
}
"""


def main():
    with sync_playwright() as p:
        browser = p.chromium.launch(executable_path=CHROME, args=["--no-sandbox"])
        ctx = browser.new_context(viewport={"width": 1366, "height": 768},
                                  record_video_dir="docs/demo/out", record_video_size={"width": 1366, "height": 768})
        page = ctx.new_page()

        def say(text, wait=2200):
            page.evaluate(CAPTION_JS, text)
            page.wait_for_timeout(wait)

        def pick(label, option):
            page.get_by_role("combobox", name=label).click()
            page.get_by_role("option", name=option, exact=True).click()
            page.wait_for_timeout(700)

        page.goto(BASE + "/employees")
        page.wait_for_selector("table tr.mat-mdc-row")
        say("ACME Salary Management: the HR manager's replacement for salary spreadsheets", 3200)
        say("10,000 seeded employees across 8 countries. Paging, sorting and filtering run on the server", 3200)

        search = page.get_by_test_id("search")
        search.click()
        search.press_sequentially("sharma", delay=90)
        page.wait_for_timeout(900)
        say("Search by name, email or employee code (debounced, so one request after typing stops)", 2600)

        pick("Country", "India")
        say("Filter by country. Salaries are always shown in the employee's own currency", 2600)

        page.get_by_role("button", name="Salary / year").click()
        page.wait_for_timeout(500)
        page.get_by_role("button", name="Salary / year").click()
        page.wait_for_timeout(900)
        say("Sort by salary, highest first", 2200)

        page.get_by_role("button", name="Clear filters").click()
        page.wait_for_timeout(900)

        # Add an employee, showing validation first.
        page.get_by_role("button", name="Add employee").first.click()
        page.wait_for_selector("mat-dialog-container")
        say("Adding a new employee. The form validates before anything is sent", 2400)
        page.locator("mat-dialog-actions").get_by_role("button", name="Add employee").click()
        page.wait_for_timeout(1500)

        dialog = page.locator("mat-dialog-container")
        dialog.locator("[formcontrolname=employeeCode]").fill("EMP10001")
        dialog.locator("[formcontrolname=fullName]").fill("Meera Joshi")
        dialog.locator("[formcontrolname=email]").fill("ahmed.brown.1@acme.com")
        dialog.locator("[formcontrolname=jobTitle]").fill("Software Engineer")
        dialog.locator("[formcontrolname=department]").fill("Engineering")
        dialog.get_by_role("combobox", name="Country").click()
        page.get_by_role("option", name="India (INR)").click()
        page.wait_for_timeout(500)
        say("The salary label follows the country: this one is entered in INR", 2400)
        dialog.locator("input[type=number]").fill("2600000")
        page.wait_for_timeout(500)
        page.locator("mat-dialog-actions").get_by_role("button", name="Add employee").click()
        page.wait_for_timeout(1200)
        if dialog.locator(".banner").count():
            say("Duplicate email: the API returns 409 and the form explains it", 2800)
        dialog.locator("[formcontrolname=email]").fill("meera.joshi@acme.com")
        page.locator("mat-dialog-actions").get_by_role("button", name="Add employee").click()
        page.wait_for_timeout(1500)

        search.fill("")
        search.press_sequentially("meera.joshi", delay=60)
        page.wait_for_timeout(1300)
        say("Saved, and immediately searchable", 2200)

        page.get_by_role("button", name="Edit Meera Joshi").click()
        page.wait_for_selector("mat-dialog-container")
        page.locator("mat-dialog-container input[type=number]").fill("2850000")
        say("Giving Meera a raise to ₹28,50,000", 2000)
        page.locator("mat-dialog-actions").get_by_role("button", name="Save changes").click()
        page.wait_for_timeout(1800)

        page.get_by_role("button", name="Delete Meera Joshi").click()
        page.wait_for_selector("mat-dialog-container")
        say("Deleting asks for confirmation first", 2000)
        page.locator("mat-dialog-actions").get_by_role("button", name="Delete").click()
        page.wait_for_timeout(1800)

        # Insights
        page.get_by_role("link", name="Pay insights").click()
        page.wait_for_selector("[data-testid=job-title-table] tbody tr")
        say("Pay insights: how does the org pay people?", 2600)
        say("Org-wide totals are converted to USD with static reference rates, and labelled approximate", 3200)
        say("Per country: min, median, average and max, in local currency. Median is the typical salary", 3400)
        page.get_by_text("Germany").first.click()
        page.wait_for_timeout(1200)
        page.mouse.wheel(0, 500)
        page.wait_for_timeout(800)
        say("Per job title in Germany, with the pay range and people more than 20% off the role's median", 3800)
        say("That list is what HR reviews first. Thanks for watching!", 2600)

        ctx.close()
        browser.close()


if __name__ == "__main__":
    main()
