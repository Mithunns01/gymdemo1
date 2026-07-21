# Postman Collection

Import the following collection into Postman to test the API endpoints.

## Variables

Set these Postman collection variables:
- `base_url`: `http://localhost:8080`
- `token`: (auto-populated after login)

## Requests

### 1. Authentication

#### Login (Admin)
```
POST {{base_url}}/auth/login
Content-Type: application/json

{
    "username": "admin",
    "password": "admin123"
}
```

Tests (Script):
```javascript
var jsonData = pm.response.json();
pm.collectionVariables.set("token", jsonData.data.token);
```

#### Login (Trainer)
```
POST {{base_url}}/auth/login
Content-Type: application/json

{
    "username": "trainer1",
    "password": "trainer123"
}
```

#### Login (Member)
```
POST {{base_url}}/auth/login
Content-Type: application/json

{
    "username": "member1",
    "password": "member123"
}
```

#### Register Member
```
POST {{base_url}}/auth/register
Content-Type: application/json

{
    "name": "New Member",
    "username": "newmember",
    "password": "pass123",
    "email": "new@member.com",
    "phone": "9876543220",
    "gender": "Male",
    "height": 170.0,
    "weight": 65.0,
    "assignedTrainerId": 1
}
```

### 2. Members (Authorization: Bearer {{token}})

#### Get All Members
```
GET {{base_url}}/members
```

#### Get Member by ID
```
GET {{base_url}}/members/1
```

#### Update Member
```
PUT {{base_url}}/members/1
Content-Type: application/json

{
    "name": "Updated Name",
    "phone": "1234567890",
    "email": "updated@email.com",
    "gender": "Male"
}
```

#### Delete (Deactivate) Member
```
DELETE {{base_url}}/members/1
```

#### Get Assigned Trainer
```
GET {{base_url}}/members/1/trainer
```

#### Get Active Membership
```
GET {{base_url}}/members/1/membership
```

#### Get Membership History
```
GET {{base_url}}/members/1/memberships
```

#### Get Workout Plans
```
GET {{base_url}}/members/1/workouts
```

#### Get Attendance History
```
GET {{base_url}}/members/1/attendance
```

#### Get Payment History
```
GET {{base_url}}/members/1/payments
```

#### Search Members
```
GET {{base_url}}/members/search?keyword=amit
```

#### Calculate BMI
```
POST {{base_url}}/members/bmi
Content-Type: application/json

{
    "height": 175,
    "weight": 70
}
```

#### Members by Trainer
```
GET {{base_url}}/members/by-trainer/1
```

### 3. Trainers (Authorization: Bearer {{token}})

#### Get All Trainers
```
GET {{base_url}}/trainers
```

#### Get Trainer by ID
```
GET {{base_url}}/trainers/1
```

#### Create Trainer
```
POST {{base_url}}/trainers
Content-Type: application/json

{
    "name": "New Trainer",
    "username": "newtrainer",
    "password": "trainer123",
    "email": "trainer@test.com",
    "phone": "9876543221",
    "specialization": "Cardio",
    "experience": 3,
    "bio": "Experienced cardio trainer"
}
```

#### Update Trainer
```
PUT {{base_url}}/trainers/1
Content-Type: application/json

{
    "name": "Updated Trainer",
    "specialization": "Strength Training"
}
```

#### Delete Trainer
```
DELETE {{base_url}}/trainers/1
```

#### Get Assigned Members
```
GET {{base_url}}/trainers/1/members
```

#### Create Workout Plan
```
POST {{base_url}}/trainers/workout
Content-Type: application/json

{
    "memberId": 1,
    "title": "Beginner Strength Program",
    "description": "A 4-week beginner strength training program",
    "exercises": "1. Squats 3x12\n2. Bench Press 3x10\n3. Rows 3x10\n4. Shoulder Press 3x10",
    "difficulty": "BEGINNER",
    "durationWeeks": 4,
    "notes": "Focus on form"
}
```

#### Update Workout Plan
```
PUT {{base_url}}/trainers/workout/1
Content-Type: application/json

{
    "title": "Updated Workout",
    "status": "COMPLETED"
}
```

#### Get Trainer Workouts
```
GET {{base_url}}/trainers/1/workouts
```

#### Trainer Member Count
```
GET {{base_url}}/trainers/member-count
```

### 4. Admin (Authorization: Bearer {{token}})

#### Get All Plans
```
GET {{base_url}}/admin/plans
```

#### Create Plan
```
POST {{base_url}}/admin/plans
Content-Type: application/json

{
    "name": "Premium Monthly",
    "description": "Premium access with personal trainer",
    "durationDays": 30,
    "price": 1999.00
}
```

#### Update Plan
```
PUT {{base_url}}/admin/plans/1
Content-Type: application/json

{
    "name": "Updated Plan",
    "price": 1499.00
}
```

#### Renew Membership
```
POST {{base_url}}/admin/renew
Content-Type: application/json

{
    "memberId": 1,
    "planId": 1,
    "paymentMode": "CARD",
    "transactionId": "TXN123456"
}
```

#### Expiring Memberships
```
GET {{base_url}}/admin/members/expiring?days=30
```

#### Get All Payments
```
GET {{base_url}}/admin/payments
```

#### Record Payment
```
POST {{base_url}}/admin/payments
Content-Type: application/json

{
    "memberId": 1,
    "amount": 999.00,
    "paymentMode": "CASH",
    "notes": "Monthly membership payment"
}
```

#### Get Revenue
```
GET {{base_url}}/admin/payments/revenue?start=2024-01-01&end=2024-12-31
```

#### Revenue Report
```
GET {{base_url}}/admin/payments/report?year=2024
```

#### Mark Attendance (Admin)
```
POST {{base_url}}/admin/attendance
Content-Type: application/json

{
    "memberId": 1,
    "notes": "Morning session"
}
```

#### Daily Attendance Report
```
GET {{base_url}}/admin/attendance/daily?date=2024-01-15
```

#### Update Member Progress
```
POST {{base_url}}/admin/members/1/progress
Content-Type: application/json

{
    "weight": 75.0,
    "bmi": 24.5,
    "notes": "Improved strength"
}
```

#### Dashboard
```
GET {{base_url}}/admin/dashboard
```

### 5. Attendance (Authorization: Bearer {{token}})

#### Mark Attendance (Member)
```
POST {{base_url}}/attendance
Content-Type: application/json

{
    "memberId": 1
}
```

#### Get Attendance History
```
GET {{base_url}}/attendance/1
```

### 6. Payments (Authorization: Bearer {{token}})

#### Get All Payments
```
GET {{base_url}}/payments
```

#### Record Payment
```
POST {{base_url}}/payments
Content-Type: application/json

{
    "memberId": 1,
    "amount": 999.00,
    "paymentMode": "CASH"
}
```

#### Revenue Report
```
GET {{base_url}}/payments/report?year=2024
```

### 7. Workout Plans (Authorization: Bearer {{token}})

#### Create Workout Plan
```
POST {{base_url}}/workout
Content-Type: application/json

{
    "memberId": 1,
    "title": "Weight Loss Program",
    "exercises": "Cardio + HIIT",
    "difficulty": "INTERMEDIATE"
}
```

#### Update Workout Plan
```
PUT {{base_url}}/workout/1
Content-Type: application/json

{
    "title": "Updated Program",
    "status": "COMPLETED"
}
```

### 8. Dashboard (Authorization: Bearer {{token}})

```
GET {{base_url}}/dashboard
```

### 9. Export (Authorization: Bearer {{token}})

```
GET {{base_url}}/export/attendance?date=2024-01-15
```

