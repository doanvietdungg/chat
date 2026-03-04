# Plans

Thư mục này chứa các file kế hoạch triển khai (`*.plan.md`) được sinh bởi prompt **Phân tích & Lập kế hoạch**.

## Workflow

```
1. Prompt "Phân tích & Lập kế hoạch"
        │
        ▼
   .github/plans/{feature}.plan.md  ← plan chi tiết
        │
        ▼
2. Agent "Implement Plan" (GPT-4.1)
        │
        ▼
   Code được triển khai vào codebase
```

## Naming convention

```
{verb}-{resource}.plan.md
```

Ví dụ:
- `add-reactions.plan.md`
- `refactor-chat-service.plan.md`
- `add-notification-system.plan.md`
